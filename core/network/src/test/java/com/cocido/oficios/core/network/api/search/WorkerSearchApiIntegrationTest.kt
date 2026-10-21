package com.cocido.oficios.core.network.api.search

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.test.NetworkTestFactory
import com.cocido.oficios.core.network.toNetworkAppError
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

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

    @Test
    fun `search workers empty list`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("search/workers_search_empty.json")),
        )
        val response = api.searchWorkers(rubroId = 3L, pageNumber = 1, pageSize = 20)
        assertTrue(response.data.isEmpty())
        assertEquals(0, response.metadata?.count)
    }

    @Test
    fun `search workers 401 maps to Unauthorized`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(NetworkTestFactory.loadFixture("errors/error_401.json")),
        )
        try {
            api.searchWorkers(rubroId = 3L)
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Unauthorized)
        }
    }

    @Test
    fun `search workers 500 maps to Network`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody(NetworkTestFactory.loadFixture("errors/error_500.json")),
        )
        try {
            api.searchWorkers(rubroId = 3L)
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }
}
