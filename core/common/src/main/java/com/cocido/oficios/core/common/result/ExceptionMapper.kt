package com.cocido.oficios.core.common.result

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import javax.net.ssl.SSLException

fun Throwable.toAppError(): AppError {
    if (hasSslOrCertificateCause()) {
        return AppError.Network(message = "Secure connection could not be established")
    }

    return when (this) {
        is ClientValidationException -> AppError.Validation(message = message)
        is SocketTimeoutException -> AppError.Network(message = "Request timed out")
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
    is AppError.Network -> message
    is AppError.Validation -> message
    is AppError.Unauthorized -> message
    is AppError.Forbidden -> message
    is AppError.NotFound -> message
    is AppError.Conflict -> message
    is AppError.Unknown -> "Algo salió mal. Intentá de nuevo."
}

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
