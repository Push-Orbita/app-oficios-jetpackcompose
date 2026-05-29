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
            "Secure connection could not be established",
            error.userMessage(),
        )
    }

    @Test
    fun `unknown error hides technical message`() {
        val error = RuntimeException("Internal boom").toAppError()

        assertTrue(error is AppError.Unknown)
        assertEquals(
            "Something went wrong. Please try again.",
            error.userMessage(),
        )
    }
}
