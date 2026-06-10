package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.toAppError
import java.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

private val errorJson = Json { ignoreUnknownKeys = true }

fun Throwable.toNetworkAppError(): AppError {
    if (this is HttpException) {
        return toHttpAppError()
    }
    if (this is IOException) {
        return toAppError()
    }
    return toAppError()
}

private fun HttpException.toHttpAppError(): AppError {
    val body = response()?.errorBody()?.string().orEmpty()
    val message = parseErrorMessage(body) ?: message()

    return when (code()) {
        400, 422 -> AppError.Validation(message = message, fieldErrors = parseFieldErrors(body))
        401 -> AppError.Unauthorized(message = message.ifBlank { "Credenciales inválidas" })
        403 -> AppError.Forbidden(message = message.ifBlank { "Acceso denegado" })
        404 -> AppError.NotFound(message = message.ifBlank { "Recurso no encontrado" })
        409 -> AppError.Conflict(message = message.ifBlank { "El recurso ya existe" })
        in 500..599 -> AppError.Network(message = "Error del servidor. Intentá más tarde.", code = code())
        else -> AppError.Network(message = message, code = code())
    }
}

private fun parseErrorMessage(body: String): String? {
    if (body.isBlank()) return null
    return runCatching {
        val json = errorJson.parseToJsonElement(body)
        when (json) {
            is JsonObject -> {
                json["message"]?.jsonPrimitive?.content
                    ?: json["error"]?.jsonPrimitive?.content
                    ?: json["detail"]?.jsonPrimitive?.content
            }
            else -> null
        }
    }.getOrNull()
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
