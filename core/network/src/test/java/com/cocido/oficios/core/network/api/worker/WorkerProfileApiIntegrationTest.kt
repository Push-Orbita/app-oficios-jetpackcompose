package com.cocido.oficios.core.network.api.worker

import com.cocido.oficios.core.network.test.NetworkTestFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkerProfileApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: WorkerProfileApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createWorkerProfileApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `get trabajador-oficio by id hits correct path and deserializes`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("worker/worker_profile_ok.json")),
        )

        val response = api.getWorkerProfile(id = 6L)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/trabajador-oficio/6", request.path)

        assertEquals(6L, response.id)
        assertEquals("VALIDADO", response.estado)
        assertEquals("43453548", response.documento)
        assertNotNull(response.usuario.persona)
        assertEquals("Matias", response.usuario.persona?.nombres)
        assertTrue(response.rubros.isNotEmpty())
        assertNull(response.reputacion)
        assertTrue(response.certificados.isEmpty())
        assertTrue(response.galeriaTrabajos.isEmpty())
        assertTrue(response.domicilios.isEmpty())
    }
}

