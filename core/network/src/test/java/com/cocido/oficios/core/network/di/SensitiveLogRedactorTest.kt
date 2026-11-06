package com.cocido.oficios.core.network.di

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveLogRedactorTest {

    @Test
    fun `redacts JSON password fields case-insensitively`() {
        val input = """{"email":"a@b.com","contrasena":"Secret1!","password":"x","nuevaContrasena":"y"}"""
        val out = SensitiveLogRedactor.redact(input)
        assertFalse(out.contains("Secret1!"))
        assertFalse(out.contains("\"password\":\"x\""))
        assertFalse(out.contains("nuevaContrasena\":\"y\""))
        assertTrue(out.contains("\"contrasena\":\"***\""))
        assertTrue(out.contains("\"email\":\"a@b.com\""))
    }

    @Test
    fun `redacts token refreshToken and otp codigo`() {
        val input = """{"token":"abc","refreshToken":"def","codigo":"123456","otp":"999999"}"""
        val out = SensitiveLogRedactor.redact(input)
        assertEquals(
            """{"token":"***","refreshToken":"***","codigo":"***","otp":"***"}""",
            out,
        )
    }

    @Test
    fun `redacts dni and documento`() {
        val input = """{"dni":"30111222","documento":"30122333","nombre":"Ana"}"""
        val out = SensitiveLogRedactor.redact(input)
        assertTrue(out.contains("\"dni\":\"***\""))
        assertTrue(out.contains("\"documento\":\"***\""))
        assertTrue(out.contains("\"nombre\":\"Ana\""))
    }

    @Test
    fun `redacts form urlencoded and bearer`() {
        val form = "email=a@b.com&contrasena=Secret1!&refreshToken=tok"
        val out = SensitiveLogRedactor.redact(form)
        assertFalse(out.contains("Secret1!"))
        assertFalse(out.contains("tok"))
        assertTrue(out.contains("contrasena=***"))
        assertTrue(out.contains("email=a@b.com"))

        val auth = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.payload.sig"
        val authOut = SensitiveLogRedactor.redact(auth)
        assertTrue(authOut.contains("Bearer ***"))
        assertFalse(authOut.contains("eyJhbGci"))
    }

    @Test
    fun `multipart aware logger redacts value line after disposition`() {
        val lines = mutableListOf<String>()
        val logger = SensitiveLogRedactor.MultipartAwareLogger { lines += it }
        logger.log("""Content-Disposition: form-data; name="contrasena"""")
        logger.log("")
        logger.log("SuperSecretPassword1!")
        logger.log("Content-Disposition: form-data; name=\"email\"")
        logger.log("")
        logger.log("user@test.com")

        assertTrue(lines[0].contains("name=\"contrasena\""))
        assertEquals("***", lines[2])
        assertFalse(lines.joinToString("\n").contains("SuperSecretPassword1!"))
        assertTrue(lines.last().contains("user@test.com"))
    }
}