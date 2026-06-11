package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.test.NetworkTestFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClientProfileApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ClientProfileApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createClientProfileApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `post domicilios sends payload`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("profile/domicilio_ok.json")),
        )

        api.createDomicilio(
            CreateDomicilioRequestDto(
                barrioId = 5,
                personaId = 99,
                calle = "San Martín",
                numero = "123",
                favorito = true,
            ),
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/domicilios", request.path)
        val body = NetworkTestFactory.json.decodeFromString<CreateDomicilioRequestDto>(request.body.readUtf8())
        assertEquals(5L, body.barrioId)
        assertEquals(99L, body.personaId)
    }

    @Test
    fun `post contacto sends payload`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("profile/contacto_ok.json")),
        )

        api.createContacto(
            CreateContactoRequestDto(
                nombre = "Juan",
                telefonoWhatsapp = "1122334455",
                email = "juan@test.com",
                favorito = true,
            ),
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/contacto", request.path)
    }
}
