package com.cocido.oficios.core.common.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.cert.CertPathValidatorException
import javax.net.ssl.SSLHandshakeException

class ExceptionMapperTest {

    @Test
    fun `ssl handshake maps to friendly network error`() {
        val error = SSLHandshakeException("Handshake failed").apply {
            initCause(CertPathValidatorException("Trust anchor for certification path not found."))
        }.toAppError()

        assertTrue(error is AppError.Network)
        assertEquals(
            "No pudimos establecer una conexión segura. Intentá nuevamente más tarde.",
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
            "Algo salió mal. Intentá de nuevo.",
            error.userMessage(),
        )
    }

    @Test
    fun `backend implementation details never reach the user`() {
        val error = AppError.Validation(
            "El trabajador ya tiene un rubro principal configurado. Use POST /full/secundario.",
        )

        assertEquals(
            "No pudimos guardar los cambios. Revisá los datos e intentá nuevamente.",
            error.userMessage(),
        )
    }

    @Test
    fun `database outage uses service unavailable copy`() {
        val error = AppError.Validation("La consulta a la base de datos falló.")

        assertEquals(
            "El servicio no está disponible en este momento. Intentá más tarde.",
            error.userMessage(),
        )
    }
}
