package com.cocido.oficios.feature.home.orders

import androidx.compose.ui.graphics.Color
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.arrangedFor
import com.cocido.oficios.domain.model.withArrivedTracking
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdersListGroupingTest {

    @Test
    fun `activos quedan primero por fecha y el historial al reves`() {
        val items = listOf(
            item(1, ServiceRequestStatus.ACCEPTED, BookingStatus.CONFIRMED, "2026-09-28T10:00:00Z"),
            item(2, ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED, "2026-09-20T10:00:00Z"),
            item(3, ServiceRequestStatus.PENDING, null, "2026-09-26T10:00:00Z"),
            item(4, ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED, "2026-09-21T10:00:00Z"),
        )
        assertEquals(listOf(3L, 1L), items.arrangedFor(OrdersListFilter.ACTIVE).map { it.id })
        assertEquals(listOf(4L, 2L), items.arrangedFor(OrdersListFilter.HISTORY).map { it.id })
    }

    @Test
    fun `confirmado en camino y en curso quedan en activos`() {
        val items = listOf(
            item(5, ServiceRequestStatus.ACCEPTED, BookingStatus.CONFIRMED, "2026-09-25T10:00:00Z"),
            item(6, ServiceRequestStatus.PENDING, BookingStatus.PENDING, "2026-09-28T14:00:00Z"),
            item(11, ServiceRequestStatus.ACCEPTED, BookingStatus.IN_PROGRESS, "2026-09-29T10:00:00Z"),
            item(15, ServiceRequestStatus.ACCEPTED, BookingStatus.EN_ROUTE, "2026-10-05T15:00:00Z"),
            item(17, ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED, "2026-09-21T10:00:00Z"),
            item(18, ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED, "2026-09-20T10:00:00Z"),
        )
        assertEquals(listOf(5L, 6L, 11L, 15L), items.arrangedFor(OrdersListFilter.ACTIVE).map { it.id })
        assertEquals(listOf(17L, 18L), items.arrangedFor(OrdersListFilter.HISTORY).map { it.id })
    }

    @Test
    fun `LLEGUE abre la misma UI que un pedido en curso`() {
        val enRoute = order(BookingStatus.EN_ROUTE)
        val arrived = enRoute.withArrivedTracking("LLEGUE")
        assertEquals(BookingStatus.IN_PROGRESS, arrived.bookingStatus)
        assertEquals(ClientPrimaryAction.CompletionCode, clientPrimaryAction(
            status = arrived.status,
            bookingStatus = arrived.bookingStatus,
            hasContact = true,
            alreadyRated = false,
        ))
        assertEquals(BookingStatus.FINISHED, order(BookingStatus.FINISHED).withArrivedTracking("LLEGUE").bookingStatus)
        assertEquals(BookingStatus.EN_ROUTE, enRoute.withArrivedTracking("EN_CAMINO").bookingStatus)
    }

    @Test
    fun `en camino entra en la linea y cancelado no`() {
        assertTrue(orderTimeline(ServiceRequestStatus.ACCEPTED, BookingStatus.EN_ROUTE).contains(OrderTimelineStep.EN_ROUTE))
        assertTrue(orderTimeline(ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED).isEmpty())
        assertEquals(0, orderTimelineActiveIndex(ServiceRequestStatus.PENDING, null))
    }

    @Test
    fun `chips de estado superan aa`() {
        listOf(
            ServiceRequestStatus.PENDING to null,
            ServiceRequestStatus.ACCEPTED to BookingStatus.CONFIRMED,
            ServiceRequestStatus.ACCEPTED to BookingStatus.EN_ROUTE,
            ServiceRequestStatus.ACCEPTED to BookingStatus.IN_PROGRESS,
            ServiceRequestStatus.ACCEPTED to BookingStatus.FINISHED,
            ServiceRequestStatus.CANCELLED to BookingStatus.CANCELLED,
        ).forEach { (status, booking) ->
            val visual = orderStatusVisual(status, booking)
            val ratio = contrast(visual.content, visual.container)
            assertTrue("$status $booking $ratio", ratio >= 4.5)
        }
    }

    private fun item(
        id: Long,
        status: ServiceRequestStatus,
        booking: BookingStatus?,
        sortKey: String,
    ) = ServiceOrderListItem(
        id = id,
        counterpartName = "Ana",
        subRubroLabel = "Plomería",
        status = status,
        bookingStatus = booking,
        scheduledLabel = sortKey,
        addressLabel = "Calle 1",
        scheduledSortKey = sortKey,
    )

    private fun order(bookingStatus: BookingStatus) = ServiceOrder(
        id = 11,
        clientName = "Ana",
        workerName = "Luis",
        workerId = 5,
        rubroLabel = "Hogar",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.ACCEPTED,
        description = "",
        addressLabel = "Calle 1",
        contactName = "Ana",
        contactPhone = "370",
        bookingId = 11,
        bookingStatus = bookingStatus,
    )
}

private fun contrast(foreground: Color, background: Color): Double {
    val lighter = maxOf(foreground.relativeLuminance(), background.relativeLuminance())
    val darker = minOf(foreground.relativeLuminance(), background.relativeLuminance())
    return (lighter + 0.05) / (darker + 0.05)
}

private fun Color.relativeLuminance(): Double {
    fun channel(value: Float): Double {
        val c = value.toDouble()
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
}
