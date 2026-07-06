package com.cocido.oficios.core.network.api.auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class AuthApiSendVerificationEmailIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var authApi: AuthApi

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
        authApi = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `send verification email accepts 201 with message body`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("""{"message":"Código de verificación reenviado a tu correo"}""")
                .addHeader("Content-Type", "application/json"),
        )

        val response = authApi.sendVerificationEmail()

        assertEquals("Código de verificación reenviado a tu correo", response.message)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/send-verification-email", request.path)
    }
}
