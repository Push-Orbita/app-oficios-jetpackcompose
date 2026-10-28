package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.test.NetworkTestFactory
import com.cocido.oficios.core.network.toNetworkAppError
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

class WorkGalleryApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: WorkGalleryApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createWorkGalleryApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `delete 200 empty body is success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))
        api.delete(12L)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/galeria-trabajo/12", request.path)
    }

    @Test
    fun `delete 200 plain text body is success`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/plain")
                .setBody("Foto eliminada"),
        )
        api.delete(13L)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun `delete 400 not owner maps validation`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"code":"VA01","message":"No es su galería"}"""),
        )
        try {
            api.delete(99L)
            fail("expected HttpException")
        } catch (error: HttpException) {
            val mapped = error.toNetworkAppError()
            assertTrue(mapped is AppError.Validation)
            assertEquals("No es su galería", mapped.message)
        }
    }
}
