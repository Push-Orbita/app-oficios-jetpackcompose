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
}
