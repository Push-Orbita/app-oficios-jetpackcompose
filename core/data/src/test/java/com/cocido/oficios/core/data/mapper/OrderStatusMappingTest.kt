package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.orders.dto.BookingSimpleDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.arrangedFor
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(BookingStatus.EXPIRED, "EXPIRADA".toBookingStatus())
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
            solicitud(19, "ACEPTADA", "EXPIRADA"),
        ).map { it.toListItem(isWorkerViewing = true) }

        val now = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, DisplayDateTime.ARGENTINA)
        assertEquals(listOf(15L, 11L, 9L, 6L, 5L), items.arrangedFor(OrdersListFilter.ACTIVE, now).map { it.id })
        assertEquals(listOf(19L, 18L, 17L), items.arrangedFor(OrdersListFilter.HISTORY, now).map { it.id })
        assertEquals(BookingStatus.IN_PROGRESS, items.first { it.id == 11L }.bookingStatus)
        assertEquals(BookingStatus.IN_PROGRESS, items.first { it.id == 15L }.bookingStatus)
        assertEquals(BookingStatus.EXPIRED, items.first { it.id == 19L }.bookingStatus)
    }

    @Test
    fun `booking FINALIZADA manda sobre una solicitud desfasada`() {
        assertEquals(BookingStatus.FINISHED, "FINALIZADO".toBookingStatus())
        for (estado in listOf("PENDIENTE", "ACEPTADA", "FINALIZADA", "FINALIZADO", "")) {
            val order = SolicitudServicioDto(
                id = 28,
                estado = estado,
                booking = BookingSimpleDto(id = 28, estado = "FINALIZADA", puedeCancelar = true),
            ).toDomain()
            assertEquals("estado=$estado", ServiceRequestStatus.ACCEPTED, order.status)
            assertEquals(BookingStatus.FINISHED, order.bookingStatus)
            assertFalse("booking finalizado no se cancela", order.canCancelBooking)
        }
    }

    @Test
    fun `solicitud cancelada o expirada no se pisa con el booking`() {
        assertEquals(ServiceRequestStatus.CANCELLED, resolveRequestStatus("CANCELADA", BookingStatus.FINISHED))
        assertEquals(ServiceRequestStatus.EXPIRED, resolveRequestStatus("EXPIRADA", BookingStatus.CONFIRMED))
        assertEquals(ServiceRequestStatus.PENDING, resolveRequestStatus("PENDIENTE", BookingStatus.PENDING))
        assertEquals(ServiceRequestStatus.PENDING, resolveRequestStatus("PENDIENTE", null))
        assertEquals(ServiceRequestStatus.COUNTER_OFFER, resolveRequestStatus("CONTRAPROPUESTA", null))
    }

    @Test
    fun `listado con booking FINALIZADA queda aceptado y en historial`() {
        val item = solicitud(28, "PENDIENTE", "FINALIZADA").toListItem(isWorkerViewing = true)
        assertEquals(ServiceRequestStatus.ACCEPTED, item.status)
        assertEquals(BookingStatus.FINISHED, item.bookingStatus)
        val now = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, DisplayDateTime.ARGENTINA)
        assertEquals(listOf(28L), listOf(item).arrangedFor(OrdersListFilter.HISTORY, now).map { it.id })
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
