package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.contract.ImageUploadContract
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.isGenericValidationCopy
import com.cocido.oficios.core.common.result.toAppError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true }

fun Throwable.toNetworkAppError(): AppError {
    if (this is HttpException) {
        return toHttpAppError()
    }
    val chain = generateSequence(this) { it.cause }
    val networkFailure = chain.firstOrNull { it.isDefiniteConnectivityFailure() }
        ?: chain.firstOrNull { it is IOException }
    return (networkFailure ?: this).toAppError()
}

private fun Throwable.isDefiniteConnectivityFailure(): Boolean =
    this is UnknownHostException ||
        this is SocketTimeoutException ||
        this is TimeoutException

private fun HttpException.toHttpAppError(): AppError {
    val body = response()?.errorBody()?.string().orEmpty()
    val httpCode = code()
    val htmlBody = looksLikeHtmlErrorBody(body)
    if (httpCode == 413 || (htmlBody && httpCode in 400..499)) {
        return AppError.Network(
            message = ImageUploadContract.PAYLOAD_TOO_LARGE_MESSAGE,
            code = httpCode,
        )
    }
    if (htmlBody && httpCode in 500..599) {
        return AppError.Network(
            message = "Error del servidor. Intente más tarde.",
            code = httpCode,
        )
    }
    val backendCode = parseErrorCode(body)
    val fieldErrors = parseFieldErrors(body)
    val parsedMessage = parseErrorMessage(body)
    val message = messageForBackendCode(backendCode, parsedMessage, fieldErrors) ?: message()

    return when (httpCode) {
        400, 422 -> AppError.Validation(
            message = message.ifBlank { "Revise los datos ingresados" },
            fieldErrors = fieldErrors,
            backendCode = backendCode,
        )
        401 -> AppError.Unauthorized(
            message = message.ifBlank { "Credenciales inválidas" },
            backendCode = backendCode,
        )
        403 -> AppError.Forbidden(
            message = message.ifBlank { "Acceso denegado" },
            backendCode = backendCode,
        )
        404 -> AppError.NotFound(
            message = message.ifBlank { "Recurso no encontrado" },
            backendCode = backendCode,
        )
        409, 410, 423 -> AppError.Conflict(
            message = message.ifBlank { "No se pudo completar la acción" },
            backendCode = backendCode,
        )
        429 -> AppError.Network(
            message = message.ifBlank { "Espere un momento e intente de nuevo." },
            code = httpCode,
            backendCode = backendCode,
        )
        in 500..599 -> AppError.Network(
            message = serverErrorMessage(backendCode, parsedMessage),
            code = httpCode,
            backendCode = backendCode,
        )
        else -> AppError.Network(
            message = message.ifBlank { "No pudimos completar la solicitud. Intente de nuevo." },
            code = httpCode,
            backendCode = backendCode,
        )
    }
}

internal fun looksLikeHtmlErrorBody(body: String): Boolean {
    val trimmed = body.trimStart().lowercase()
    if (trimmed.isBlank()) return false
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) return false
    return trimmed.startsWith("<") ||
        trimmed.contains("<html") ||
        trimmed.contains("<center>") ||
        trimmed.contains("nginx")
}

internal fun parseErrorCode(body: String): String? {
    if (body.isBlank()) return null
    val json = runCatching { errorJson.parseToJsonElement(body) }.getOrNull() as? JsonObject ?: return null
    val fromDetails = json["errorDetails"]?.jsonObject?.get("code")?.jsonPrimitive?.contentOrNull
    val fromRoot = json["code"]?.jsonPrimitive?.contentOrNull
    val fromArray = (json["errorDetails"]?.jsonObject?.get("message") as? JsonArray)
        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
        ?.firstOrNull { it in knownBackendMessages }
    return listOfNotNull(fromDetails, fromRoot, fromArray)
        .firstOrNull { it.isNotBlank() && it != "Unauthorized" && it != "Bad Request" && it != "Not Found" }
}

internal fun parseErrorMessage(body: String): String? {
    if (body.isBlank()) return null
    val json = runCatching { errorJson.parseToJsonElement(body) }.getOrNull() as? JsonObject
        ?: return null

    val candidates = listOfNotNull(
        runCatching {
            parseNestedDetail(json["errorDetails"]?.jsonObject?.get("details")?.jsonPrimitive?.content)
        }.getOrNull(),
        runCatching { json["errorDetails"]?.jsonObject?.get("message")?.let(::extractMessageText) }.getOrNull(),
        runCatching { json["message"]?.let(::extractMessageText) }.getOrNull(),
        runCatching { json["detail"]?.jsonPrimitive?.content }.getOrNull(),
        runCatching { json["error"]?.jsonPrimitive?.takeIf { it.isString }?.content }.getOrNull(),
    )

    return candidates.firstOrNull { isUserFacingErrorMessage(it) }
}

private fun extractMessageText(element: JsonElement): String? = runCatching {
    when (element) {
        is JsonArray -> element.mapNotNull { it.jsonPrimitive.contentOrNull }
            .filter { it.isNotBlank() && it !in knownBackendMessages && !it.contains("must be", ignoreCase = true) }
            .joinToString(separator = " ")
            .ifBlank { null }
        else -> element.jsonPrimitive.contentOrNull
    }
}.getOrNull()

private fun parseNestedDetail(details: String?): String? {
    if (details.isNullOrBlank()) return null
    return runCatching {
        val nested = errorJson.parseToJsonElement(details)
        if (nested is JsonObject) {
            nested["detail"]?.jsonPrimitive?.content
        } else {
            null
        }
    }.getOrNull()
}

private fun isUserFacingErrorMessage(value: String): Boolean {
    if (value.isBlank()) return false
    return value.lowercase() !in uselessErrorMessages
}

private val uselessErrorMessages = setOf(
    "true",
    "false",
    "error",
    "unauthorized",
    "forbidden",
    "not found",
    "conflict",
    "bad request",
)

private val knownBackendMessages = mapOf(
    "COMPLETION_CODE_REQUIRED" to "Ingrese el código de 6 dígitos que ve el cliente en el pedido.",
    "COMPLETION_CODE_INVALID" to "Ese código no es correcto o ya cambió. Pídale al cliente el que ve ahora en su app.",
    "COMPLETION_CODE_EXPIRED" to "Ese código venció. La app del cliente ya muestra uno nuevo: pedile el actual.",
    "COMPLETION_CODE_LOCKED" to "Hay demasiados intentos. Espere un momento e intente de nuevo.",
    "BOOKING_INVALID_STATE" to "Todavía no se puede terminar este pedido.",
    "REPORT_ALREADY_OPEN" to "Ya denunció este perfil. Vamos a revisarlo.",
    "REPORT_REASON_INVALID" to "Seleccione un motivo de la lista.",
    "REPORT_DESCRIPTION_INVALID" to "Cuéntenos qué pasó (entre 20 y 2000 caracteres).",
    "REPORT_RATE_LIMITED" to "Envió varias denuncias seguidas. Espere un momento.",
    "ACCOUNT_DELETION_CONFIRMATION_INVALID" to "Escriba ELIMINAR para confirmar la baja.",
    "AUTH_RECENT_LOGIN_REQUIRED" to "La contraseña no es correcta.",
    "ACCOUNT_HAS_ACTIVE_BOOKINGS" to "Termine o cancele sus trabajos activos antes de eliminar la cuenta.",
    "ACCOUNT_DELETION_ALREADY_REQUESTED" to "Ya hay una solicitud de baja en curso.",
    "ACCOUNT_UNDER_ADMIN_REVIEW" to "Su cuenta está en revisión. No se puede eliminar por ahora.",
    "AGENDA_SLOT_ALREADY_RESERVED" to "Ese turno ya no está disponible. Elija otro horario.",
    "AGENDA_SLOT_NOT_AVAILABLE" to "Ese horario ya no se puede reservar.",
    "COMPLETION_CODE_RATE_LIMITED" to "Espere un momento para reenviar el código.",
    "CANCELATION_WINDOW_EXPIRED" to "Ya no se puede cancelar este servicio. El plazo de cancelación venció.",
    "DB02" to "El servicio no está disponible en este momento. Intente más tarde.",
    "SE01" to "El servicio no está disponible en este momento. Intente más tarde.",
    "VA01" to "Revise los datos ingresados",
    PHONE_CODE_DELIVERY_FAILED to PHONE_CODE_DELIVERY_FAILED_MESSAGE,
)

/** Backend: proveedor de WhatsApp/SMS no confirmó la entrega (OpenAPI 503 en enviar-codigo-telefono). */
internal const val PHONE_CODE_DELIVERY_FAILED = "VF06"
internal const val PHONE_CODE_DELIVERY_FAILED_MESSAGE =
    "No se pudo enviar el código por WhatsApp. Intente más tarde."
private const val GENERIC_SERVER_ERROR_MESSAGE = "Error del servidor. Intente más tarde."

/**
 * Los 5xx genéricos (SE01, DB02, sin código) siguen con copy genérico. Los códigos de verificación
 * de teléfono (VFxx) son errores de negocio que el usuario puede entender: se muestra su copy.
 */
private fun serverErrorMessage(backendCode: String?, parsed: String?): String {
    if (backendCode == PHONE_CODE_DELIVERY_FAILED) return PHONE_CODE_DELIVERY_FAILED_MESSAGE
    if (backendCode?.startsWith("VF") == true) {
        return parsed?.takeIf { isUserFacingErrorMessage(it) } ?: PHONE_CODE_DELIVERY_FAILED_MESSAGE
    }
    return GENERIC_SERVER_ERROR_MESSAGE
}

private fun messageForBackendCode(
    code: String?,
    parsed: String?,
    fieldErrors: Map<String, String> = emptyMap(),
): String? {
    val usefulParsed = parsed?.takeIf { it.isNotBlank() && !it.isGenericValidationCopy() }
    val fromFields = fieldErrors.values.firstOrNull { it.isNotBlank() && !it.isGenericValidationCopy() }
    if (code == "VA01") {
        return usefulParsed ?: fromFields ?: knownBackendMessages["VA01"]
    }
    knownBackendMessages[code]?.let { return it }
    if (code?.startsWith("DB") == true || code?.startsWith("SE") == true) {
        return "El servicio no está disponible en este momento. Intente más tarde."
    }
    knownBackendMessages.entries.firstOrNull { parsed?.contains(it.key) == true }?.let { return it.value }
    return usefulParsed ?: fromFields ?: parsed
}

internal fun parseFieldErrors(body: String): Map<String, String> {
    if (body.isBlank()) return emptyMap()
    return runCatching {
        val json = errorJson.parseToJsonElement(body).jsonObject
        val errorsElement = json["fieldErrors"] ?: json["errors"] ?: return emptyMap()
        if (errorsElement !is JsonObject) return emptyMap()
        errorsElement.mapNotNull { (field, value) ->
            val messages = when (value) {
                is JsonArray -> value.mapNotNull { it.jsonPrimitive.contentOrNull }
                is JsonPrimitive -> listOfNotNull(value.contentOrNull)
                else -> emptyList()
            }
            val picked = pickUserFacingFieldMessage(field, messages) ?: return@mapNotNull null
            field to picked
        }.toMap()
    }.getOrNull().orEmpty()
}

internal fun pickUserFacingFieldMessage(field: String, messages: List<String>): String? {
    val cleaned = messages.map { it.trim() }.filter { it.isNotBlank() }
    if (cleaned.isEmpty()) return null
    if (cleaned.all { message ->
            val lower = message.lowercase()
            lower.contains("should not exist") || lower.startsWith("property ")
        }
    ) {
        return null
    }
    val userFacing = cleaned.filter { message ->
        val lower = message.lowercase()
        !message.isGenericValidationCopy() &&
            !lower.contains("must be") &&
            !lower.contains("should not") &&
            !lower.startsWith("property ")
    }
    userFacing.firstOrNull()?.let { return it }
    return classValidatorFallback(field, cleaned)
}

private fun classValidatorFallback(field: String, messages: List<String>): String? {
    if (messages.isEmpty()) return null
    val lowerField = field.lowercase()
    val joined = messages.joinToString(" ").lowercase()
    if (lowerField == "codigo") {
        return "El código tiene que tener 6 dígitos"
    }
    if (lowerField == "email" && (joined.contains("email") || joined.contains("string"))) {
        return "Ingrese un correo válido"
    }
    return null
}
