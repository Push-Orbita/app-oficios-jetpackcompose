package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.remote.OrderRemoteDataSource
import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.orders.OrdersApi
import com.cocido.oficios.core.network.api.orders.dto.BookingDto
import com.cocido.oficios.core.network.api.orders.dto.CancelarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateCalificacionRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateContrapropuestaRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateSolicitudServicioRequestDto
import com.cocido.oficios.core.network.api.orders.dto.FinalizeBookingRequestDto
import com.cocido.oficios.core.network.api.orders.dto.MarcarTrackingRequestDto
import com.cocido.oficios.core.network.api.orders.dto.RechazarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSearchPageDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.TrackingBookingDto
import com.cocido.oficios.core.network.api.orders.dto.UpdateSolicitudEstadoRequestDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderRepositoryInvalidationTest {

    @Test
    fun `cancelar un pedido avisa a la lista`() = runTest {
        val api = FakeOrdersApi()
        val repository = OrderRepositoryImpl(
            OrderRemoteDataSource(api),
            object : DispatcherProvider {
                override val main: CoroutineDispatcher = Dispatchers.Unconfined
                override val io: CoroutineDispatcher = Dispatchers.Unconfined
                override val default: CoroutineDispatcher = Dispatchers.Unconfined
            },
        )
        val events = mutableListOf<Unit>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.ordersChanged.collect { events.add(Unit) }
        }

        api.fail = true
        assertTrue(repository.cancelOrderBeforeAcceptance(8L) is AppResult.Error)
        assertEquals(0, events.size)

        api.fail = false
        assertTrue(repository.cancelOrderBeforeAcceptance(8L) is AppResult.Success)
        assertEquals(1, events.size)
        job.cancel()
    }
}

private class FakeOrdersApi : OrdersApi {
    var fail = false
    private fun <T> result(value: T): T {
        if (fail) error("fallo")
        return value
    }

    override suspend fun createSolicitud(idempotencyKey: String, request: CreateSolicitudServicioRequestDto) = error("no")
    override suspend fun getMisSolicitudesCliente(): PaginatedResponseDto<SolicitudServicioSimpleDto> = error("no")
    override suspend fun getSolicitudesTrabajador() = SolicitudServicioSearchPageDto()
    override suspend fun getSolicitud(id: Long) = SolicitudServicioDto(id = id, estado = "PENDIENTE")
    override suspend fun aceptarSolicitud(id: Long) = error("no")
    override suspend fun rechazarSolicitud(id: Long, request: RechazarSolicitudRequestDto) = error("no")
    override suspend fun enviarContrapropuesta(id: Long, request: CreateContrapropuestaRequestDto): JsonElement = error("no")
    override suspend fun cancelarSolicitudConfirmada(id: Long, request: CancelarSolicitudRequestDto) = error("no")
    override suspend fun cancelarSolicitudPendiente(id: Long, request: UpdateSolicitudEstadoRequestDto) =
        result(SolicitudServicioDto(id = id, estado = "CANCELADA"))
    override suspend fun aceptarContrapropuesta(id: Long): JsonElement = error("no")
    override suspend fun rechazarContrapropuesta(id: Long): JsonElement = error("no")
    override suspend fun getBooking(id: Long) = BookingDto(id = id, estado = "PENDIENTE")
    override suspend fun resendCompletionCode(id: Long): com.cocido.oficios.core.network.api.orders.dto.ResendCompletionCodeResponseDto = error("no")
    override suspend fun finalizarBooking(id: Long, request: FinalizeBookingRequestDto) = error("no")
    override suspend fun marcarTracking(bookingId: Long, request: MarcarTrackingRequestDto): TrackingBookingDto = error("no")
    override suspend fun getTracking(bookingId: Long): JsonElement = JsonNull
    override suspend fun crearCalificacion(request: CreateCalificacionRequestDto): JsonElement = error("no")
}
