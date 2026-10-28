package com.cocido.oficios.core.network.api.agenda

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.test.NetworkTestFactory
import com.cocido.oficios.core.network.test.enqueueDisconnect
import com.cocido.oficios.core.network.test.enqueueFixture
import com.cocido.oficios.core.network.test.enqueueJson
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

class AgendaApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AgendaApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createAgendaApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `slots disponibles 200`() = runTest {
        server.enqueueFixture(200, "agenda/slots_disponibles_ok.json")
        val slots = api.getSlotsDisponibles(
            subRubroId = 10L,
            fechaDesde = "2026-10-25",
            fechaHasta = "2026-10-31",
        )
        assertEquals(1, slots.size)
        assertEquals("DISPONIBLE", slots.first().estado)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("10", request.requestUrl?.queryParameter("trabajadorOficioSubRubroId"))
    }

    @Test
    fun `slots disponibles with fecha query`() = runTest {
        server.enqueueJson(200, "[]")
        api.getSlotsDisponibles(
            subRubroId = 10L,
            fechaDesde = "2026-10-26",
            fechaHasta = "2026-10-26",
            fecha = "2026-10-26",
        )
        val request = server.takeRequest()
        assertEquals("2026-10-26", request.requestUrl?.queryParameter("fecha"))
        assertEquals("2026-10-26", request.requestUrl?.queryParameter("fechaDesde"))
    }

    @Test
    fun `slots disponibles empty`() = runTest {
        server.enqueueJson(200, "[]")
        assertTrue(api.getSlotsDisponibles(10L, "2026-10-25", "2026-10-31").isEmpty())
    }

    @Test
    fun `slots 500 maps to Network`() = runTest {
        server.enqueueFixture(500, "errors/error_500.json")
        try {
            api.getSlotsDisponibles(10L, "2026-10-25", "2026-10-31")
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `slots timeout maps to Network`() = runTest {
        val timed = NetworkTestFactory.createAgendaApi(server, timeoutMs = 80)
        server.enqueueNoResponse()
        try {
            timed.getSlotsDisponibles(10L, "2026-10-25", "2026-10-31")
            fail("expected timeout")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `slots without network maps to Network`() = runTest {
        server.enqueueDisconnect()
        try {
            api.getSlotsDisponibles(10L, "2026-10-25", "2026-10-31")
            fail("expected IOException")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }
}
