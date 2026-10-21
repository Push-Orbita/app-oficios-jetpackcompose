package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.orders.dto.BookingSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.arrangedFor
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderStatusMappingTest {

    @Test
    fun `mapea los estados vivos del booking y LLEGUE como en curso`() {
        assertEquals(BookingStatus.PENDING, "PENDIENTE".toBookingStatus())
        assertEquals(BookingStatus.CONFIRMED, "CONFIRMADA".toBookingStatus())
        assertEquals(BookingStatus.EN_ROUTE, "EN_CAMINO".toBookingStatus())
        assertEquals(BookingStatus.IN_PROGRESS, "EN_CURSO".toBookingStatus())
        assertEquals(BookingStatus.IN_PROGRESS, "LLEGUE".toBookingStatus())
        assertEquals(BookingStatus.FINISHED, "FINALIZADA".toBookingStatus())
        assertEquals(BookingStatus.CANCELLED, "CANCELADA".toBookingStatus())
    }

    @Test
    fun `el listado del trabajador deja en curso y llegada en activos`() {
        val items = listOf(
            solicitud(11, "ACEPTADA", "EN_CURSO"),
            solicitud(15, "ACEPTADA", "LLEGUE"),
            solicitud(5, "ACEPTADA", "CONFIRMADA"),
            solicitud(9, "ACEPTADA", "EN_CAMINO"),
            solicitud(6, "PENDIENTE", "PENDIENTE"),
            solicitud(18, "ACEPTADA", "FINALIZADA"),
            solicitud(17, "CANCELADA", "CANCELADA"),
        ).map { it.toListItem(isWorkerViewing = true) }

        assertEquals(listOf(6L, 15L, 11L, 9L, 5L), items.arrangedFor(OrdersListFilter.ACTIVE).map { it.id })
        assertEquals(listOf(18L, 17L), items.arrangedFor(OrdersListFilter.HISTORY).map { it.id })
        assertEquals(BookingStatus.IN_PROGRESS, items.first { it.id == 11L }.bookingStatus)
        assertEquals(BookingStatus.IN_PROGRESS, items.first { it.id == 15L }.bookingStatus)
    }

    private fun solicitud(id: Long, estado: String, bookingEstado: String) = SolicitudServicioSimpleDto(
        id = id,
        estado = estado,
        booking = BookingSimpleDto(
            id = id,
            estado = bookingEstado,
            inicioConfirmado = "2026-09-${10 + (id % 20)}T10:00:00Z",
        ),
    )
}
