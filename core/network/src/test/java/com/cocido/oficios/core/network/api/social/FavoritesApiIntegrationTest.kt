package com.cocido.oficios.core.network.api.social

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.test.NetworkTestFactory
import com.cocido.oficios.core.network.test.enqueueDisconnect
import com.cocido.oficios.core.network.test.enqueueFixture
import com.cocido.oficios.core.network.test.enqueueNoResponse
import com.cocido.oficios.core.network.toNetworkAppError
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class FavoritesApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: FavoritesApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createFavoritesApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `list 200`() = runTest {
        server.enqueueFixture(200, "favorites/favorites_ok.json")
        val page = api.getFavorites()
        assertEquals(1, page.data.size)
        assertEquals(5L, page.data.first().trabajadorOficioId)
        assertEquals("Matias Britez", page.data.first().displayName)
        assertEquals("/favoritos?pageNumber=1&pageSize=20", server.takeRequest().path)
    }

    @Test
    fun `list empty`() = runTest {
        server.enqueueFixture(200, "favorites/favorites_empty.json")
        assertTrue(api.getFavorites().data.isEmpty())
    }

    @Test
    fun `add sends Idempotency-Key`() = runTest {
        server.enqueueFixture(200, "favorites/favorito_item_ok.json")
        val item = api.add(trabajadorOficioId = 5L, idempotencyKey = "fav-qa-1")
        assertEquals(9L, item.id)
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/favoritos/trabajador-oficio/5", request.path)
        assertEquals("fav-qa-1", request.getHeader("Idempotency-Key"))
    }

    @Test
    fun `remove 204`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))
        api.remove(5L)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/favoritos/trabajador-oficio/5", request.path)
    }

    @Test
    fun `status 404 maps to NotFound`() = runTest {
        server.enqueueFixture(404, "errors/error_404.json")
        try {
            api.getStatus(999L)
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.NotFound)
        }
    }

    @Test
    fun `list 500 maps to Network`() = runTest {
        server.enqueueFixture(500, "errors/error_500.json")
        try {
            api.getFavorites()
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `list timeout maps to Network`() = runTest {
        val timed = NetworkTestFactory.createFavoritesApi(server, timeoutMs = 80)
        server.enqueueNoResponse()
        try {
            timed.getFavorites()
            fail("expected timeout")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `list without network maps to Network`() = runTest {
        server.enqueueDisconnect()
        try {
            api.getFavorites()
            fail("expected IOException")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }
}
