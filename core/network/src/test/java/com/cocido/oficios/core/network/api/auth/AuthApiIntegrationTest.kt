package com.cocido.oficios.core.network.api.auth

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.api.auth.dto.LoginRequestDto
import com.cocido.oficios.core.network.api.auth.dto.RequestPasswordResetCodeDto
import com.cocido.oficios.core.network.test.NetworkTestFactory
import com.cocido.oficios.core.network.test.enqueueDisconnect
import com.cocido.oficios.core.network.test.enqueueFixture
import com.cocido.oficios.core.network.test.enqueueJson
import com.cocido.oficios.core.network.test.enqueueNoResponse
import com.cocido.oficios.core.network.toNetworkAppError
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class AuthApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AuthApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createAuthApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `login 200 deserializes tokens and usuario nombres apellido`() = runTest {
        server.enqueueFixture(200, "auth/login_ok.json")

        val response = api.login(LoginRequestDto("qa@example.test", "secret123"))

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.qa", response.accessToken)
        assertEquals("refresh-qa-token", response.refreshToken)
        assertEquals("QA", response.usuario.persona?.nombres)
        assertEquals("Emulator", response.usuario.persona?.apellido)
        assertEquals("CLIENTE", response.usuario.rolUsuario?.nombre)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/login", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"email\""))
        assertTrue(body.contains("\"contrasena\""))
        assertTrue(!body.contains("\"password\""))
    }

    @Test
    fun `login 401 maps to Unauthorized`() = runTest {
        server.enqueueFixture(401, "errors/error_401.json")
        try {
            api.login(LoginRequestDto("qa@example.test", "bad"))
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertEquals(401, error.code())
            val mapped = error.toNetworkAppError()
            assertTrue(mapped is AppError.Unauthorized)
        }
    }

    @Test
    fun `login 500 maps to Network`() = runTest {
        server.enqueueFixture(500, "errors/error_500.json")
        try {
            api.login(LoginRequestDto("qa@example.test", "secret123"))
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertEquals(500, error.code())
            val mapped = error.toNetworkAppError()
            assertTrue(mapped is AppError.Network)
            assertEquals(500, (mapped as AppError.Network).code)
        }
    }

    @Test
    fun `login timeout maps to Network`() = runTest {
        val timedApi = NetworkTestFactory.createAuthApi(server, timeoutMs = 80)
        server.enqueueNoResponse()
        try {
            timedApi.login(LoginRequestDto("qa@example.test", "secret123"))
            fail("expected timeout")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `login without network maps to Network`() = runTest {
        val disconnected = NetworkTestFactory.createAuthApi(server)
        server.enqueueDisconnect()
        try {
            disconnected.login(LoginRequestDto("qa@example.test", "secret123"))
            fail("expected IOException")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `signup 201 keeps compound nombres without splitting`() = runTest {
        server.enqueueFixture(201, "auth/signup_ok.json")
        val text = "text/plain".toMediaType()
        val response = api.signup(
            firstName = "Matias Federico".toRequestBody(text),
            lastName = "Britez".toRequestBody(text),
            email = "nuevo@example.test".toRequestBody(text),
            password = "secret123".toRequestBody(text),
            roleId = "2".toRequestBody(text),
            document = "43453548".toRequestBody(text),
        )
        assertEquals("Matias Federico", response.usuario.persona?.nombres)
        assertEquals("Britez", response.usuario.persona?.apellido)
        assertEquals(21L, response.cliente?.id)
        assertEquals("/auth/signup", server.takeRequest().path)
    }

    @Test
    fun `me 200 deserializes current user`() = runTest {
        server.enqueueFixture(200, "auth/me_ok.json")
        val user = api.getCurrentUser()
        assertEquals(13L, user.id)
        assertEquals("qa@example.test", user.email)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/auth/me", request.path)
    }

    @Test
    fun `logout 204 succeeds`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))
        api.logout()
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/logout", request.path)
    }

    @Test
    fun `password reset request 201 with empty body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201))
        api.requestPasswordReset(RequestPasswordResetCodeDto("qa@example.test"))
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/password-reset/request-code", request.path)
        assertTrue(request.body.readUtf8().contains("qa@example.test"))
    }

    @Test
    fun `password reset 404 maps to NotFound`() = runTest {
        server.enqueueFixture(404, "errors/error_404.json")
        try {
            api.requestPasswordReset(RequestPasswordResetCodeDto("missing@example.test"))
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.NotFound)
        }
    }
}
