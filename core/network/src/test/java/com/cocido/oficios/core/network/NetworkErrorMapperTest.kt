package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.userMessage
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `parseErrorCode reads errorDetails code`() {
        val body = """
            {
              "error": true,
              "errorDetails": {
                "code": "COMPLETION_CODE_REQUIRED",
                "message": ["code must be a string"]
              }
            }
        """.trimIndent()

        assertEquals("COMPLETION_CODE_REQUIRED", parseErrorCode(body))
    }

    @Test
    fun `parseErrorMessage ignores must be technical array entries`() {
        val body = """
            {
              "error": true,
              "errorDetails": {
                "code": "COMPLETION_CODE_REQUIRED",
                "message": ["code must be a string"]
              }
            }
        """.trimIndent()

        assertEquals("COMPLETION_CODE_REQUIRED", parseErrorCode(body))
        assertEquals(null, parseErrorMessage(body))
    }

    @Test
    fun `parseErrorCode reads DB02 database outage`() {
        val body = """
            {
              "error": true,
              "errorDetails": {
                "code": "DB02",
                "message": "La consulta a la base de datos falló.",
                "details": "Unknown database 'oficiosdev'"
              }
            }
        """.trimIndent()

        assertEquals("DB02", parseErrorCode(body))
    }

    @Test
    fun `parseErrorCode reads SE01 server error`() {
        val body = """
            {
              "statusCode": 500,
              "message": "Internal server error",
              "code": "SE01"
            }
        """.trimIndent()

        assertEquals("SE01", parseErrorCode(body))
    }

    @Test
    fun `unknown host, io and timeout map to a network error`() {
        val cases = listOf(
            UnknownHostException("no host"),
            IOException("connection reset"),
            SocketTimeoutException("timeout"),
            TimeoutException("timeout"),
            RuntimeException("wrapped", UnknownHostException("no host")),
        )
        cases.forEach { throwable ->
            val error = throwable.toNetworkAppError()
            assertTrue(throwable::class.java.simpleName, error is AppError.Network)
            assertTrue(error.userMessage().isNotBlank())
        }
    }
}
