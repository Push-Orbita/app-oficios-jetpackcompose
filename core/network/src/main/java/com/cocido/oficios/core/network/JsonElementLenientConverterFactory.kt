package com.cocido.oficios.core.network

import java.lang.reflect.Type
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Red de seguridad para métodos que todavía declaran [JsonElement]: un 2xx con
 * body vacío o texto plano ("Contacto eliminado correctamente") no debe fallar
 * el parseo. El JSON válido se interpreta normal.
 *
 * La corrección de raíz es devolver [Unit] cuando el body no se usa
 * ([UnitResponseConverterFactory]).
 */
object JsonElementLenientConverterFactory : Converter.Factory() {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        if (type != JsonElement::class.java) return null
        return Converter<ResponseBody, JsonElement> { body ->
            val text = body.string()
            when {
                text.isBlank() -> JsonNull
                else -> runCatching { json.parseToJsonElement(text) }
                    .getOrElse { JsonPrimitive(text) }
            }
        }
    }
}
