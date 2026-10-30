package com.cocido.oficios.core.network

import com.cocido.oficios.core.common.contract.ImageUploadContract
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
    fun `va01 generic message does not keep nest copy and never uses photo hint`() {
        val body = """{"code":"VA01","message":"Datos de entrada no válidos."}"""
        val error = httpError(400, body).toNetworkAppError()
        assertTrue(error is AppError.Validation)
        assertEquals("Revise los datos ingresados", error.message)
        assertTrue("JPG" !in error.message)
        assertTrue("Datos de entrada no válidos" !in error.message)
        assertEquals("Revise los datos ingresados", error.userMessage())
    }

    @Test
    fun `va01 without message uses form copy`() {
        val body = """{"code":"VA01"}"""
        val error = httpError(400, body).toNetworkAppError()
        assertEquals("Revise los datos ingresados", error.message)
        assertTrue("JPG" !in error.message)
    }

    @Test
    fun `va01 keeps specific backend message`() {
        val body = """{"code":"VA01","message":"No es su galería"}"""
        val error = httpError(400, body).toNetworkAppError()
        assertEquals("No es su galería", error.message)
    }

    @Test
    fun `prod envelope fieldErrors codigo surfaces spanish copy not nest va01`() {
        val body = """
            {
              "error": true,
              "code": "VA01",
              "message": "Datos de entrada no válidos.",
              "fieldErrors": {
                "codigo": [
                  "El código debe tener 6 dígitos",
                  "codigo must be a string",
                  "codigo must be longer than or equal to 6 characters",
                  "codigo should not be empty"
                ]
              },
              "traceId": "14ad0bdc-3d39-4d65-9c17-1087c3a5f056"
            }
        """.trimIndent()
        val error = httpError(400, body).toNetworkAppError()
        assertTrue(error is AppError.Validation)
        val validation = error as AppError.Validation
        assertEquals("VA01", validation.backendCode)
        assertEquals("El código debe tener 6 dígitos", validation.fieldErrors["codigo"])
        assertEquals("El código debe tener 6 dígitos", validation.message)
        assertEquals("El código debe tener 6 dígitos", error.userMessage())
        assertTrue("Datos de entrada no válidos" !in error.userMessage())
    }

    @Test
    fun `prod envelope extra code alias does not hide missing codigo`() {
        val body = """
            {
              "error": true,
              "code": "VA01",
              "message": "Datos de entrada no válidos.",
              "fieldErrors": {
                "code": ["property code should not exist"],
                "codigo": [
                  "El código debe tener 6 dígitos",
                  "codigo must be a string",
                  "codigo should not be empty"
                ]
              }
            }
        """.trimIndent()
        val error = httpError(400, body).toNetworkAppError() as AppError.Validation
        assertEquals("El código debe tener 6 dígitos", error.fieldErrors["codigo"])
        assertTrue(error.fieldErrors["code"].isNullOrBlank())
        assertEquals("El código debe tener 6 dígitos", error.userMessage())
    }

    @Test
    fun `http 500 maps to Network with code 500`() {
        val error = httpError(500, NetworkTestFactory.loadFixture("errors/error_500.json"))
            .toNetworkAppError()
        assertTrue(error is AppError.Network)
        assertEquals(500, (error as AppError.Network).code)
        assertEquals("Error del servidor. Intente más tarde.", error.message)
    }

    @Test
    fun `http 413 nginx html maps to payload too large never error de red`() {
        val body = """
            <html>
            <head><title>413 Request Entity Too Large</title></head>
            <body>
            <center><h1>413 Request Entity Too Large</h1></center>
            <hr><center>nginx/1.24.0</center>
            </body>
            </html>
        """.trimIndent()
        val error = httpError(413, body, "text/html").toNetworkAppError()
        assertTrue(error is AppError.Network)
        assertEquals(413, (error as AppError.Network).code)
        assertEquals(ImageUploadContract.PAYLOAD_TOO_LARGE_MESSAGE, error.message)
        assertEquals(ImageUploadContract.PAYLOAD_TOO_LARGE_MESSAGE, error.userMessage())
        assertTrue("Error de red" !in error.userMessage())
        assertTrue(looksLikeHtmlErrorBody(body))
    }

    @Test
    fun `http 400 html maps to payload too large`() {
        val body = "<html><head><title>400</title></head><body>nginx</body></html>"
        val error = httpError(400, body, "text/html").toNetworkAppError()
        assertEquals(ImageUploadContract.PAYLOAD_TOO_LARGE_MESSAGE, error.message)
        assertTrue(error is AppError.Network)
        assertTrue("Error de red" !in error.userMessage())
    }

    @Test
    fun `http 418 empty body never says error de red`() {
        val error = httpError(418, "", "text/plain").toNetworkAppError()
        assertTrue(error is AppError.Network)
        assertEquals(418, (error as AppError.Network).code)
        assertTrue("Error de red" !in error.message)
        assertTrue("Error de red" !in error.userMessage())
    }

    @Test
    fun `cancelation window expired maps to user friendly message`() {
        val body = """{"code":"CANCELATION_WINDOW_EXPIRED","message":"Plazo de cancelación vencido"}"""
        val error = httpError(400, body).toNetworkAppError()
        assertTrue(error is AppError.Validation)
        assertEquals("Ya no se puede cancelar este servicio. El plazo de cancelación venció.", error.message)
    }

    private fun httpError(code: Int, body: String, mediaType: String = "application/json"): HttpException {
        val responseBody = body.toResponseBody(mediaType.toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
