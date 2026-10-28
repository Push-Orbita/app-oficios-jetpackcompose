package com.cocido.oficios.core.network

import java.lang.reflect.Type
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Cualquier 2xx cuyo body no se usa (DELETE, PATCH/POST con texto plano o vacío)
 * debe declarar [Unit]. kotlinx.serialization falla con "Foto eliminada" /
 * "Contacto eliminado correctamente". Registrarlo **antes** del converter JSON
 * descarta el body.
 */
object UnitResponseConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        if (type != Unit::class.java) return null
        return Converter<ResponseBody, Unit> { body ->
            body.close()
            Unit
        }
    }
}
