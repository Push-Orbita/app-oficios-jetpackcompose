package com.cocido.oficios.core.network.api.search

import com.cocido.oficios.core.network.test.NetworkTestFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class WorkerSearchApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: WorkerSearchApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createWorkerSearchApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search workers deserializes paginated response`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("search/workers_search_ok.json")),
        )

        val response = api.searchWorkers(rubroId = 3L, pageNumber = 1, pageSize = 20)

        assertEquals(1, response.data.size)
        val worker = response.data.first()
        assertEquals(12L, worker.id)
        assertEquals("VALIDADO", worker.estado)
        assertEquals("Carlos", worker.usuario?.persona?.nombres)
        assertEquals(4.8, worker.reputacion?.promedio)
        assertEquals(1, response.metadata?.pageNumber)
        assertEquals(1, response.metadata?.totalPages)

        val request = server.takeRequest()
        assertNotNull(request.requestUrl?.queryParameter("rubroId"))
        assertEquals("3", request.requestUrl?.queryParameter("rubroId"))
    }
}
