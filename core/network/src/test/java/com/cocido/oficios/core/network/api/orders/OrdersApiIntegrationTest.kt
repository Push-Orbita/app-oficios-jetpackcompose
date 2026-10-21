package com.cocido.oficios.core.network.api.orders

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.network.api.orders.dto.CancelarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateSolicitudServicioRequestDto
import com.cocido.oficios.core.network.api.orders.dto.UpdateSolicitudEstadoRequestDto
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

class OrdersApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OrdersApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createOrdersApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `mis solicitudes cliente 200 deserializes numeric favorito`() = runTest {
        server.enqueueFixture(200, "orders/mis_solicitudes_cliente_ok.json")
        val page = api.getMisSolicitudesCliente()
        assertEquals(1, page.data.size)
        val item = page.data.first()
        assertEquals(5L, item.id)
        assertEquals("PENDIENTE", item.estado)
        assertTrue(item.domicilio!!.favorito)
        assertEquals("QA", item.cliente?.usuario?.persona?.nombres)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/solicitud-servicio/mis-solicitudes-cliente", request.path)
    }

    @Test
    fun `mis solicitudes cliente empty list`() = runTest {
        server.enqueueFixture(200, "orders/mis_solicitudes_empty.json")
        val page = api.getMisSolicitudesCliente()
        assertTrue(page.data.isEmpty())
        assertEquals(0, page.metadata?.count)
    }

    @Test
    fun `trabajador search nested data shape`() = runTest {
        server.enqueueFixture(200, "orders/solicitudes_trabajador_ok.json")
        val resolved = api.getSolicitudesTrabajador().resolved()
        assertEquals(5L, resolved.trabajador?.id)
        assertEquals(1, resolved.solicitudes.size)
        assertEquals("Cambio de canilla", resolved.solicitudes.first().descripcion)
    }

    @Test
    fun `trabajador search empty solicitudes`() = runTest {
        server.enqueueFixture(200, "orders/solicitudes_trabajador_empty.json")
        assertTrue(api.getSolicitudesTrabajador().resolved().solicitudes.isEmpty())
    }

    @Test
    fun `get solicitud 200`() = runTest {
        server.enqueueFixture(200, "orders/solicitud_detail_ok.json")
        val detail = api.getSolicitud(5L)
        assertEquals(5L, detail.id)
        assertEquals("PENDIENTE", detail.estado)
        assertEquals("/solicitud-servicio/5", server.takeRequest().path)
    }

    @Test
    fun `get solicitud 404 maps to NotFound`() = runTest {
        server.enqueueFixture(404, "errors/error_404.json")
        try {
            api.getSolicitud(999L)
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.NotFound)
        }
    }

    @Test
    fun `cancel pending 400 without motivo maps to Validation`() = runTest {
        server.enqueueFixture(400, "errors/error_400.json")
        try {
            api.cancelarSolicitudConfirmada(5L, CancelarSolicitudRequestDto(motivoCancelacion = ""))
            fail("expected HttpException")
        } catch (error: HttpException) {
            val mapped = error.toNetworkAppError()
            assertTrue(mapped is AppError.Validation)
        }
    }

    @Test
    fun `cancel pending 200 sends estado CANCELADA`() = runTest {
        server.enqueueFixture(200, "orders/solicitud_detail_ok.json")
        api.cancelarSolicitudPendiente(5L, UpdateSolicitudEstadoRequestDto(estado = "CANCELADA"))
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/solicitud-servicio/5", request.path)
        assertTrue(request.body.readUtf8().contains("CANCELADA"))
    }

    @Test
    fun `create solicitud sends Idempotency-Key`() = runTest {
        server.enqueueFixture(201, "orders/solicitud_detail_ok.json")
        api.createSolicitud(
            idempotencyKey = "qa-idem-1",
            request = CreateSolicitudServicioRequestDto(
                trabajadorOficioSubRubroId = 10L,
                domicilioId = 6L,
                contactoId = 5L,
                descripcion = "qa",
            ),
        )
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("qa-idem-1", request.getHeader("Idempotency-Key"))
    }

    @Test
    fun `list 500 maps to Network`() = runTest {
        server.enqueueFixture(500, "errors/error_500.json")
        try {
            api.getMisSolicitudesCliente()
            fail("expected HttpException")
        } catch (error: HttpException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `list timeout maps to Network`() = runTest {
        val timed = NetworkTestFactory.createOrdersApi(server, timeoutMs = 80)
        server.enqueueNoResponse()
        try {
            timed.getMisSolicitudesCliente()
            fail("expected timeout")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }

    @Test
    fun `list without network maps to Network`() = runTest {
        server.enqueueDisconnect()
        try {
            api.getMisSolicitudesCliente()
            fail("expected IOException")
        } catch (error: IOException) {
            assertTrue(error.toNetworkAppError() is AppError.Network)
        }
    }
}
