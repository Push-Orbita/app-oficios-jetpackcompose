package com.cocido.oficios.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkErrorMapperTest {

    @Test
    fun `parseErrorMessage prefers nested backend detail`() {
        val body = """
            {
              "error": true,
              "errorDetails": {
                "code": "VA01",
                "message": "Datos de entrada no válidos.",
                "details": "{\"detail\":\"Formato no válido para frente. Aceptados: JPG, PNG, WEBP, GIF\"}"
              }
            }
        """.trimIndent()

        assertEquals(
            "Formato no válido para frente. Aceptados: JPG, PNG, WEBP, GIF",
            parseErrorMessage(body),
        )
    }

    @Test
    fun `parseErrorMessage extracts text when errorDetails message is an array`() {
        val body = """
            {
              "error": true,
              "errorDetails": {
                "message": ["Ingresá solo el número local, sin + ni código de país. Ejemplo: 3705246344"],
                "error": "Bad Request",
                "statusCode": 400
              }
            }
        """.trimIndent()

        assertEquals(
            "Ingresá solo el número local, sin + ni código de país. Ejemplo: 3705246344",
            parseErrorMessage(body),
        )
    }
}
