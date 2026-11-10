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
    fun `redacts snake_case access_token refresh_token and reset_token`() {
        val input =
            """{"access_token":"jwt-access","refresh_token":"jwt-refresh","reset_token":"jwt-reset","expires_in":"3600"}"""
        val out = SensitiveLogRedactor.redact(input)
        assertEquals(
            """{"access_token":"***","refresh_token":"***","reset_token":"***","expires_in":"3600"}""",
            out,
        )
    }

    @Test
    fun `redacts confirm password fields`() {
        val input =
            """{"contrasena":"Secret1!","confirmarContrasena":"Secret1!","confirmPassword":"Secret1!"}"""
        val out = SensitiveLogRedactor.redact(input)
        assertFalse(out.contains("Secret1!"))
        assertTrue(out.contains("\"contrasena\":\"***\""))
        assertTrue(out.contains("\"confirmarContrasena\":\"***\""))
        assertTrue(out.contains("\"confirmPassword\":\"***\""))
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
        assertTrue(authOut.contains("Authorization: ***"))
        assertFalse(authOut.contains("eyJhbGci"))
        assertFalse(authOut.contains("Bearer eyJ"))
    }

    @Test
    fun `redacts refresh_token in form and Authorization without Bearer prefix`() {
        val form = "refresh_token=jwt-refresh&access_token=jwt-access"
        val out = SensitiveLogRedactor.redact(form)
        assertEquals("refresh_token=***&access_token=***", out)

        val raw = "Authorization: jwt-access-raw"
        val authOut = SensitiveLogRedactor.redact(raw)
        assertEquals("Authorization: ***", authOut)
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

    @Test
    fun `redacts multipart password when entire body is one OkHttp log message`() {
        val body = listOf(
            "--okhttp-boundary",
            "Content-Disposition: form-data; name=\"nombres\"",
            "",
            "Ana",
            "--okhttp-boundary",
            "Content-Disposition: form-data; name=\"contrasena\"",
            "",
            "SuperSecretPassword1!",
            "--okhttp-boundary",
            "Content-Disposition: form-data; name=\"email\"",
            "",
            "user@test.com",
            "--okhttp-boundary--",
        ).joinToString("\r\n")

        val lines = mutableListOf<String>()
        val logger = SensitiveLogRedactor.MultipartAwareLogger { lines += it }
        logger.log(body)
        logger.log("--> END POST")

        assertEquals(2, lines.size)
        val out = lines[0]
        assertFalse(out.contains("SuperSecretPassword1!"))
        assertTrue(out.contains("name=\"contrasena\""))
        assertTrue(out.contains("***"))
        assertTrue(out.contains("Ana"))
        assertTrue(out.contains("user@test.com"))
        assertEquals("--> END POST", lines[1])
    }

    @Test
    fun `redacts unquoted multipart password and skips extra Content-Type header`() {
        val body = listOf(
            "--okhttp-boundary",
            "Content-Disposition: form-data; name=contrasena",
            "Content-Type: text/plain; charset=utf-8",
            "",
            "SuperSecretPassword1!",
            "--okhttp-boundary--",
        ).joinToString("\n")

        val out = SensitiveLogRedactor.redact(body)
        assertFalse(out.contains("SuperSecretPassword1!"))
        assertTrue(out.contains("name=contrasena"))
        assertTrue(out.contains("***"))
    }
}
