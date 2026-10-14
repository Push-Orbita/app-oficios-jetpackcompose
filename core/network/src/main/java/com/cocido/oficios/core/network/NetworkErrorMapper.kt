package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.toAppError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
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
    val backendCode = parseErrorCode(body)
    val parsedMessage = parseErrorMessage(body)
    val message = messageForBackendCode(backendCode, parsedMessage) ?: message()

    return when (code()) {
        400, 422 -> AppError.Validation(
            message = message.ifBlank { "Revisá los datos ingresados" },
            fieldErrors = parseFieldErrors(body),
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
            message = message.ifBlank { "Esperá un momento e intentá de nuevo." },
            code = code(),
            backendCode = backendCode,
        )
        in 500..599 -> AppError.Network(
            message = "Error del servidor. Intentá más tarde.",
            code = code(),
            backendCode = backendCode,
        )
        else -> AppError.Network(
            message = message.ifBlank { "Error de red" },
            code = code(),
            backendCode = backendCode,
        )
    }
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
    "COMPLETION_CODE_REQUIRED" to "Ingresá el código de 6 dígitos que ve el cliente en el pedido.",
    "COMPLETION_CODE_INVALID" to "Ese código no es correcto. Pedile al cliente que lo confirme.",
    "COMPLETION_CODE_EXPIRED" to "El código venció. Pedile al cliente que lo reenvíe.",
    "COMPLETION_CODE_LOCKED" to "Hay demasiados intentos. Esperá un momento e intentá de nuevo.",
    "BOOKING_INVALID_STATE" to "Todavía no se puede terminar este pedido.",
    "REPORT_ALREADY_OPEN" to "Ya denunciaste este perfil. Vamos a revisarlo.",
    "REPORT_REASON_INVALID" to "Elegí un motivo de la lista.",
    "REPORT_DESCRIPTION_INVALID" to "Contanos qué pasó (entre 20 y 2000 caracteres).",
    "REPORT_RATE_LIMITED" to "Enviaste varias denuncias seguidas. Esperá un momento.",
    "ACCOUNT_DELETION_CONFIRMATION_INVALID" to "Escribí ELIMINAR para confirmar la baja.",
    "AUTH_RECENT_LOGIN_REQUIRED" to "La contraseña no es correcta.",
    "ACCOUNT_HAS_ACTIVE_BOOKINGS" to "Terminá o cancelá tus trabajos activos antes de eliminar la cuenta.",
    "ACCOUNT_DELETION_ALREADY_REQUESTED" to "Ya hay una solicitud de baja en curso.",
    "ACCOUNT_UNDER_ADMIN_REVIEW" to "Tu cuenta está en revisión. No se puede eliminar por ahora.",
    "AGENDA_SLOT_ALREADY_RESERVED" to "Ese turno ya no está disponible. Elegí otro horario.",
    "AGENDA_SLOT_NOT_AVAILABLE" to "Ese horario ya no se puede reservar.",
    "COMPLETION_CODE_RATE_LIMITED" to "Esperá un momento para reenviar el código.",
    "DB02" to "El servicio no está disponible en este momento. Intentá más tarde.",
    "SE01" to "El servicio no está disponible en este momento. Intentá más tarde.",
)

private fun messageForBackendCode(code: String?, parsed: String?): String? {
    knownBackendMessages[code]?.let { return it }
    if (code?.startsWith("DB") == true || code?.startsWith("SE") == true) {
        return "El servicio no está disponible en este momento. Intentá más tarde."
    }
    knownBackendMessages.entries.firstOrNull { parsed?.contains(it.key) == true }?.let { return it.value }
    return parsed
}

private fun parseFieldErrors(body: String): Map<String, String> {
    if (body.isBlank()) return emptyMap()
    return runCatching {
        val json = errorJson.parseToJsonElement(body).jsonObject
        val errors = json["errors"]?.jsonObject ?: return emptyMap()
        errors.mapValues { (_, value) ->
            when {
                value.jsonArray.isNotEmpty() -> value.jsonArray.first().jsonPrimitive.content
                value.jsonPrimitive.isString -> value.jsonPrimitive.content
                else -> value.toString()
            }
        }
    }.getOrNull().orEmpty()
}
