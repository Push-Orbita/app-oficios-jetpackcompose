package com.cocido.oficios.core.common.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException

class ExceptionMapperTest {

    @Test
    fun `ssl handshake maps to friendly network error`() {
        val error = SSLHandshakeException("Handshake failed").apply {
            initCause(CertPathValidatorException("Trust anchor for certification path not found."))
        }.toAppError()

        assertTrue(error is AppError.Network)
        assertEquals(
            "No pudimos establecer una conexión segura. Intente nuevamente más tarde.",
            error.userMessage(),
        )
    }

    @Test
    fun `client validation maps to validation error`() {
        val error = ClientValidationException("Formato no válido").toAppError()

        assertTrue(error is AppError.Validation)
        assertEquals("Formato no válido", error.userMessage())
    }

    @Test
    fun `validation fieldErrors surface the first useful message`() {
        val error = AppError.Validation(
            message = "Datos de entrada no válidos.",
            fieldErrors = mapOf("fotos" to "Formato no válido. Aceptados: JPG, PNG, WEBP, GIF"),
            backendCode = "VA01",
        )
        assertEquals(
            "Formato no válido. Aceptados: JPG, PNG, WEBP, GIF",
            error.userMessage(),
        )
    }

    @Test
    fun `generic nest va01 is never shown as the only copy`() {
        val error = AppError.Validation(
            message = "Datos de entrada no válidos.",
            backendCode = "VA01",
        )
        assertEquals(
            "No pudimos guardar los cambios. Revise los datos e intente nuevamente.",
            error.userMessage(),
        )
        assertTrue("Datos de entrada no válidos" !in error.userMessage())
        assertEquals(
            VerificationCodeCopy.EMAIL,
            error.toOtpCodeMessage(VerificationCodeCopy.EMAIL),
        )
    }

    @Test
    fun `unknown error hides technical message`() {
        val error = RuntimeException("Internal boom").toAppError()

        assertTrue(error is AppError.Unknown)
        assertEquals(
            "Algo salió mal. Intente de nuevo.",
            error.userMessage(),
        )
    }

    @Test
    fun `backend implementation details never reach the user`() {
        val error = AppError.Validation(
            "El trabajador ya tiene un rubro principal configurado. Use POST /full/secundario.",
        )

        assertEquals(
            "No pudimos guardar los cambios. Revise los datos e intente nuevamente.",
            error.userMessage(),
        )
    }

    @Test
    fun `offline and timeouts map to a connection error`() {
        assertEquals(
            "No pudimos conectarnos. Revise su conexión e intente nuevamente.",
            UnknownHostException("offline").toAppError().userMessage(),
        )
        assertEquals(
            "No pudimos conectarnos. Revise su conexión e intente nuevamente.",
            IOException("reset").toAppError().userMessage(),
        )
        assertEquals(
            "La conexión tardó demasiado. Intente nuevamente.",
            SocketTimeoutException("read").toAppError().userMessage(),
        )
        assertEquals(
            "La conexión tardó demasiado. Intente nuevamente.",
            TimeoutException("call").toAppError().userMessage(),
        )
    }

    @Test
    fun `database outage uses service unavailable copy`() {
        val error = AppError.Validation("La consulta a la base de datos falló.")

        assertEquals(
            "El servicio no está disponible en este momento. Intente más tarde.",
            error.userMessage(),
        )
    }

    @Test
    fun `http 413 never surfaces as error de red`() {
        val error = AppError.Network(message = "HTTP 413 Request Entity Too Large", code = 413)
        assertEquals(
            "El archivo es demasiado pesado. Pruebe con uno más liviano.",
            error.userMessage(),
        )
        assertTrue("Error de red" !in error.userMessage())
    }

    @Test
    fun `http response with blank network copy is not error de red`() {
        val error = AppError.Network(message = "Error de red", code = 502)
        assertEquals("No pudimos completar la solicitud. Intente de nuevo.", error.userMessage())
        assertTrue("Error de red" !in error.userMessage())
    }
}
