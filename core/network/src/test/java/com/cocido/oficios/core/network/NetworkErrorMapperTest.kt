package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.core.network.test.NetworkTestFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

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

    @Test
    fun `http 401 maps to Unauthorized`() {
        val error = httpError(401, NetworkTestFactory.loadFixture("errors/error_401.json"))
            .toNetworkAppError()
        assertTrue(error is AppError.Unauthorized)
    }

    @Test
    fun `http 403 maps to Forbidden`() {
        val body = """{"statusCode":403,"message":"Forbidden","error":"Forbidden"}"""
        assertTrue(httpError(403, body).toNetworkAppError() is AppError.Forbidden)
    }

    @Test
    fun `http 404 maps to NotFound`() {
        val error = httpError(404, NetworkTestFactory.loadFixture("errors/error_404.json"))
            .toNetworkAppError()
        assertTrue(error is AppError.NotFound)
    }

    @Test
    fun `http 400 maps to Validation`() {
        val error = httpError(400, NetworkTestFactory.loadFixture("errors/error_400.json"))
            .toNetworkAppError()
        assertTrue(error is AppError.Validation)
    }

    @Test
    fun `va01 generic message becomes a useful photo format hint`() {
        val body = """{"code":"VA01","message":"Datos de entrada no válidos."}"""
        val error = httpError(400, body).toNetworkAppError()
        assertTrue(error is AppError.Validation)
        assertEquals(
            "El archivo no es un JPG, PNG, WEBP o GIF válido. Elija otra foto.",
            error.message,
        )
    }

    @Test
    fun `va01 keeps specific backend message`() {
        val body = """{"code":"VA01","message":"No es su galería"}"""
        val error = httpError(400, body).toNetworkAppError()
        assertEquals("No es su galería", error.message)
    }

    @Test
    fun `http 500 maps to Network with code 500`() {
        val error = httpError(500, NetworkTestFactory.loadFixture("errors/error_500.json"))
            .toNetworkAppError()
        assertTrue(error is AppError.Network)
        assertEquals(500, (error as AppError.Network).code)
        assertEquals("Error del servidor. Intente más tarde.", error.message)
    }

    private fun httpError(code: Int, body: String): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
