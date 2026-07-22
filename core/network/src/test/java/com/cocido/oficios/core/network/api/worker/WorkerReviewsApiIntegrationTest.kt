package com.cocido.oficios.core.network.api.worker

import com.cocido.oficios.core.network.test.NetworkTestFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkerReviewsApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: WorkerReviewsApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createWorkerReviewsApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search calificaciones hits correct path and deserializes`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("worker/worker_reviews_ok.json")),
        )

        val response = api.searchReviews(
            trabajadorOficioId = 6L,
            pageNumber = 1,
            pageSize = 5,
        )

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path?.startsWith("/calificaciones/search") == true)

        assertEquals("6", request.requestUrl?.queryParameter("trabajadorOficioId"))
        assertEquals("1", request.requestUrl?.queryParameter("pageNumber"))
        assertEquals("5", request.requestUrl?.queryParameter("pageSize"))

        assertTrue(response.data.isEmpty())
    }
}

