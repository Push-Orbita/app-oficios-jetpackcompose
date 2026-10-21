package com.cocido.oficios.core.network.api.notifications

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class NotificationsApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: NotificationsApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createNotificationsApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `list 200 deserializes bilingual fields`() = runTest {
        server.enqueueFixture(200, "notifications/notifications_ok.json")
        val page = api.getNotifications(pageSize = 20, pageNumber = 1, soloNoLeidas = true)
        assertEquals(1, page.data.size)
        val item = page.data.first()
        assertEquals(101L, item.id)
        assertEquals("ORDER_NEW_REQUEST", item.type)
        assertEquals("NUEVA_SOLICITUD", item.tipo)
        assertEquals("Nueva solicitud", item.title)
        assertFalse(item.leido == true)
        assertEquals("oficios://orders/5", item.deepLink)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("20", request.requestUrl?.queryParameter("pageSize"))
        assertEquals("true", request.requestUrl?.queryParameter("soloNoLeidas"))
    }

    @Test
    fun `list empty`() = runTest {
        server.enqueueFixture(200, "notifications/notifications_empty.json")
        assertTrue(api.getNotifications().data.isEmpty())
    }

    @Test
    fun `list 401 maps to Unauthorized`() = runTest {
        server.enqueueFixture(401, "errors/error_401.json")
        try {
            api.getNotifications()
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Unauthorized)
        }
    }

    @Test
    fun `list 500 maps to Network`() = runTest {
        server.enqueueFixture(500, "errors/error_500.json")
        try {
            api.getNotifications()
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `list timeout maps to Network`() = runTest {
        val timed = NetworkTestFactory.createNotificationsApi(server, timeoutMs = 80)
        server.enqueueNoResponse()
        try {
            timed.getNotifications()
            fail("expected timeout")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `list without network maps to Network`() = runTest {
        server.enqueueDisconnect()
        try {
            api.getNotifications()
            fail("expected IOException")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }
}
