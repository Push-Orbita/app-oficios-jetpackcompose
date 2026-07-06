package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.remote.ClientProfileRemoteDataSource
import com.cocido.oficios.core.data.test.NetworkTestHelper
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClientProfileRepositoryIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ClientProfileRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = NetworkTestHelper.createClientProfileApi(server)
        repository = ClientProfileRepositoryImpl(ClientProfileRemoteDataSource(api))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `saveDomicilio returns success`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestHelper.loadFixture("profile/domicilio_ok.json")),
        )

        val result = repository.saveDomicilio(
            ClientDomicilioRequest(
                barrioId = 1,
                personaId = 2,
                calle = "Calle Falsa",
            ),
        )

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `saveContacto maps validation error`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setBody("""{"message":"Teléfono inválido"}"""),
        )

        val result = repository.saveContacto(
            ClientContactoRequest(
                nombre = "Juan",
                telefonoWhatsapp = "abc",
            ),
        )

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.Validation)
    }
}
