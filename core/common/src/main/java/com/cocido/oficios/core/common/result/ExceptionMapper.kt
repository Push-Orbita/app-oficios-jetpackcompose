package com.cocido.oficios.core.common.result

import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import javax.net.ssl.SSLException

fun Throwable.toAppError(): AppError {
    if (hasSslOrCertificateCause()) {
        return AppError.Network(message = "Secure connection could not be established")
    }

    return when (this) {
        is ClientValidationException -> AppError.Validation(message = message)
        is SocketTimeoutException,
        is TimeoutException,
        -> AppError.Network(message = "Request timed out")
        is UnknownHostException -> AppError.Network(message = "No internet connection")
        is SSLException -> AppError.Network(message = "Secure connection could not be established")
        is CertPathValidatorException,
        is CertificateException,
        -> AppError.Network(message = "Secure connection could not be established")
        is IOException -> AppError.Network(message = "Network error")
        else -> AppError.Unknown()
    }
}

fun AppError.userMessage(): String = when (this) {
    is AppError.Network -> when {
        code == 413 || message.isPayloadTooLargeCopy() -> ImageUploadContract.PAYLOAD_TOO_LARGE_MESSAGE
        code != null -> {
            val professional = message.toProfessionalMessage(
                fallback = "No pudimos completar la solicitud. Intente de nuevo.",
            )
            if (professional.equals("Error de red", ignoreCase = true) ||
                professional.equals("Network error", ignoreCase = true) ||
                professional.startsWith("No pudimos conectarnos")
            ) {
                "No pudimos completar la solicitud. Intente de nuevo."
            } else {
                professional
            }
        }
        else -> message.toProfessionalMessage(
            fallback = "No pudimos conectarnos. Revise su conexión e intente nuevamente.",
        )
    }
    is AppError.Validation -> {
        val fromFields = fieldErrors.values.firstOrNull { value ->
            value.isNotBlank() &&
                !value.containsTechnicalDetail() &&
                !value.isGenericValidationCopy()
        }
        (fromFields ?: message).toProfessionalMessage(
            fallback = "No pudimos guardar los cambios. Revise los datos e intente nuevamente.",
        )
    }
    is AppError.Unauthorized -> message.toProfessionalMessage(
        fallback = "Su sesión venció. Vuelva a ingresar para continuar.",
    )
    is AppError.Forbidden -> message.toProfessionalMessage(
        fallback = "No tiene permiso para realizar esta acción.",
    )
    is AppError.NotFound -> message.toProfessionalMessage(
        fallback = "No encontramos la información solicitada. Actualice la pantalla e intente nuevamente.",
    )
    is AppError.Conflict -> message.toProfessionalMessage(
        fallback = "Ese dato ya está registrado. Revíselo e intente nuevamente.",
    )
    is AppError.Unknown -> "Algo salió mal. Intente de nuevo."
}

fun AppError.validationField(vararg keys: String): String? {
    val errors = (this as? AppError.Validation)?.fieldErrors ?: return null
    return keys.firstNotNullOfOrNull { key ->
        errors.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
            ?.takeIf { it.isNotBlank() && !it.isGenericValidationCopy() }
    }
}

fun AppError.toOtpCodeMessage(fallback: String): String {
    validationField("codigo", "code")?.let { return it }
    if (this is AppError.Validation &&
        (backendCode.equals("VA01", ignoreCase = true) || message.isGenericValidationCopy())
    ) {
        return fallback
    }
    return userMessage()
}

fun String.isGenericValidationCopy(): Boolean {
    val value = trim().lowercase().trimEnd('.')
    return value == "datos de entrada no válidos" ||
        value == "datos de entrada no validos"
}

object VerificationCodeCopy {
    const val EMAIL =
        "Ese código no es válido o ya venció. Revise el correo o toque Reenviar código."
    const val PHONE = "Ese código no es válido o ya venció. Pida uno nuevo."
    const val RESET = "Ese código no es válido o ya venció. Solicite uno nuevo."
}

private fun String.toProfessionalMessage(fallback: String): String {
    val normalized = trim()
    if (normalized.isBlank()) return fallback
    if (normalized.isGenericValidationCopy()) return fallback
    if (normalized.isInfrastructureFailure()) {
        return "El servicio no está disponible en este momento. Intente más tarde."
    }
    if (normalized.containsTechnicalDetail()) return fallback
    return when (normalized.lowercase()) {
        "network error", "no internet connection", "error de red" ->
            "No pudimos conectarnos. Revise su conexión e intente nuevamente."
        "request timed out" -> "La conexión tardó demasiado. Intente nuevamente."
        "secure connection could not be established" ->
            "No pudimos establecer una conexión segura. Intente nuevamente más tarde."
        else -> normalized
    }
}

private fun String.containsTechnicalDetail(): Boolean {
    val value = lowercase()
    return technicalMessagePatterns.any(value::contains)
}

private fun String.isPayloadTooLargeCopy(): Boolean {
    val value = lowercase()
    return value.contains("413") ||
        value.contains("entity too large") ||
        value.contains("payload too large") ||
        value.contains("request entity too large") ||
        value.contains("demasiado pesado")
}

private fun String.isInfrastructureFailure(): Boolean {
    val value = lowercase()
    return value.contains("consulta a la base de datos") ||
        value.contains("unknown database") ||
        value.contains("base de datos fall")
}

private val technicalMessagePatterns = listOf(
    "post /",
    "get /",
    "patch /",
    "put /",
    "delete /",
    "dto",
    "exception",
    "stack trace",
    "sql",
    "prisma",
    "foreign key",
    "constraint",
    "registro no encontrado en la base de datos",
    "consulta a la base de datos",
    "unknown database",
    "use post",
    "use patch",
    "endpoint",
)

private fun Throwable.hasSslOrCertificateCause(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        when (current) {
            is SSLException,
            is CertPathValidatorException,
            is CertificateException,
            -> return true
        }
        if (current.message?.contains("Trust anchor", ignoreCase = true) == true) {
            return true
        }
        current = current.cause
    }
    return false
}

suspend fun <T> runCatchingApp(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (throwable: Throwable) {
    AppResult.Error(throwable.toAppError())
}
