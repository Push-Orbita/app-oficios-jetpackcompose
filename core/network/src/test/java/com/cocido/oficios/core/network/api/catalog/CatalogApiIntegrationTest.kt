package com.cocido.oficios.core.network.api.catalog

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.test.NetworkTestFactory
import com.cocido.oficios.core.network.test.enqueueDisconnect
import com.cocido.oficios.core.network.test.enqueueFixture
import com.cocido.oficios.core.network.test.enqueueNoResponse
import com.cocido.oficios.core.network.toNetworkAppError
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class CatalogApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: CatalogApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createCatalogApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search categories 200`() = runTest {
        server.enqueueFixture(200, "catalog/categorias_ok.json")
        val page = api.searchCategories()
        assertEquals(2, page.data.size)
        assertEquals("Hogar y Construccion", page.data.first().nombre)
        assertEquals("/categorias/search?pageSize=100&pageNumber=1", server.takeRequest().path)
    }

    @Test
    fun `search categories empty`() = runTest {
        server.enqueueFixture(200, "catalog/categorias_empty.json")
        assertTrue(api.searchCategories().data.isEmpty())
    }

    @Test
    fun `search categories 500 maps to Network`() = runTest {
        server.enqueueFixture(500, "errors/error_500.json")
        try {
            api.searchCategories()
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `search categories timeout maps to Network`() = runTest {
        val timed = NetworkTestFactory.createCatalogApi(server, timeoutMs = 80)
        server.enqueueNoResponse()
        try {
            timed.searchCategories()
            fail("expected timeout")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `search categories without network maps to Network`() = runTest {
        server.enqueueDisconnect()
        try {
            api.searchCategories()
            fail("expected IOException")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }
}
