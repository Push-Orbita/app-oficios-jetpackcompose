package com.cocido.oficios.domain.model

import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.Clock
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdersListClassificationTest {

    private val zone = DisplayDateTime.ARGENTINA
    private val now = nowInArgentina(clockAt(2026, 10, 21, 11, 11))

    @Test
    fun `september and october confirmed slots are history on 21 oct`() {
        val sept = order(1, BookingStatus.CONFIRMED, "2026-09-30T10:00:00")
        val oct = order(2, BookingStatus.CONFIRMED, "2026-10-05T12:00:00")
        assertTrue(sept.isHistoryOrder(now))
        assertTrue(oct.isHistoryOrder(now))
        assertEquals(listOf(2L, 1L), listOf(sept, oct).arrangedFor(OrdersListFilter.HISTORY, now).map { it.id })
        assertTrue(listOf(sept, oct).arrangedFor(OrdersListFilter.ACTIVE, now).isEmpty())
    }

    @Test
    fun `pending past slot goes to history without inventing expired status`() {
        val pending = order(
            id = 3,
            booking = BookingStatus.PENDING,
            start = "2026-09-30T10:00:00",
            status = ServiceRequestStatus.PENDING,
        )
        assertTrue(pending.isHistoryOrder(now))
        assertEquals(ServiceRequestStatus.PENDING, pending.status)
        assertEquals(BookingStatus.PENDING, pending.bookingStatus)
    }

    @Test
    fun `in progress and en route stay active even if the slot ended`() {
        val inProgress = order(4, BookingStatus.IN_PROGRESS, "2026-09-30T10:00:00")
        val enRoute = order(5, BookingStatus.EN_ROUTE, "2026-10-05T12:00:00")
        assertFalse(inProgress.isHistoryOrder(now))
        assertFalse(enRoute.isHistoryOrder(now))
        assertEquals(
            listOf(5L, 4L),
            listOf(inProgress, enRoute).arrangedFor(OrdersListFilter.ACTIVE, now).map { it.id },
        )
    }

    @Test
    fun `future confirmed stays active and during slot is still active`() {
        val future = order(6, BookingStatus.CONFIRMED, "2026-10-21T15:00:00", end = "2026-10-21T16:00:00")
        val during = order(7, BookingStatus.CONFIRMED, "2026-10-21T11:00:00", end = "2026-10-21T12:00:00")
        assertFalse(future.isHistoryOrder(now))
        assertFalse(during.isHistoryOrder(now))
    }

    @Test
    fun `same day after exclusive end goes to history`() {
        val ended = order(8, BookingStatus.CONFIRMED, "2026-10-21T09:00:00", end = "2026-10-21T10:00:00")
        assertTrue(ended.isHistoryOrder(now))
    }

    @Test
    fun `undated pending stays active`() {
        val undated = order(9, booking = null, start = null, status = ServiceRequestStatus.PENDING)
        assertFalse(undated.isHistoryOrder(now))
    }

    @Test
    fun `cancelled and finished stay in history`() {
        val cancelled = order(
            id = 10,
            booking = BookingStatus.CANCELLED,
            start = "2026-10-22T10:00:00",
            status = ServiceRequestStatus.CANCELLED,
        )
        val finished = order(11, BookingStatus.FINISHED, "2026-10-22T10:00:00")
        assertTrue(cancelled.isHistoryOrder(now))
        assertTrue(finished.isHistoryOrder(now))
    }

    private fun clockAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Clock {
        val instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant()
        return Clock.fixed(instant, zone)
    }

    private fun order(
        id: Long,
        booking: BookingStatus?,
        start: String?,
        status: ServiceRequestStatus = ServiceRequestStatus.ACCEPTED,
        end: String? = null,
    ) = ServiceOrderListItem(
        id = id,
        counterpartName = "Ana",
        subRubroLabel = "Plomería",
        status = status,
        bookingStatus = booking,
        scheduledLabel = null,
        addressLabel = "Av. 25 de Mayo 450",
        rubroLabel = "Plomería",
        scheduledSortKey = start,
        scheduledEndKey = end,
    )
}
