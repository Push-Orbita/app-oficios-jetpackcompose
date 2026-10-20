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
}
