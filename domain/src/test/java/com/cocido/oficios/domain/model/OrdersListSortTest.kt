package com.cocido.oficios.domain.model

import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.LocalDate
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class OrdersListSortTest {

    private val today = LocalDate.of(2026, 10, 21)
    private val now = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, DisplayDateTime.ARGENTINA)

    @Test
    fun `newest first orders days and items by booking date`() {
        val items = listOf(
            item(1, "2026-09-30T10:00:00"),
            item(2, "2026-10-26T09:00:00"),
            item(3, "2026-10-05T12:00:00"),
        )
        assertEquals(listOf(2L, 3L, 1L), items.sortedWith(ordersListComparator(OrdersListSort.NEWEST_FIRST)).map { it.id })

        val groups = groupAgendaAppointments(items, today)
        assertEquals(
            listOf("26 de octubre", "5 de octubre", "30 de septiembre"),
            groups.map { it.headerLabel },
        )
        assertEquals(listOf(2L, 3L, 1L), groups.flatMap { it.items }.map { it.id })
    }

    @Test
    fun `oldest first orders days and items by booking date`() {
        val items = listOf(
            item(1, "2026-09-30T10:00:00"),
            item(2, "2026-10-26T09:00:00"),
            item(3, "2026-10-05T12:00:00"),
        )
        assertEquals(listOf(1L, 3L, 2L), items.sortedWith(ordersListComparator(OrdersListSort.OLDEST_FIRST)).map { it.id })

        val groups = groupAgendaAppointments(items, today, sort = OrdersListSort.OLDEST_FIRST)
        assertEquals(
            listOf("30 de septiembre", "5 de octubre", "26 de octubre"),
            groups.map { it.headerLabel },
        )
        assertEquals(listOf(1L, 3L, 2L), groups.flatMap { it.items }.map { it.id })
    }

    @Test
    fun `same booking date breaks ties by id descending in both directions`() {
        val morningLow = item(2, "2026-10-05T10:00:00")
        val morningHigh = item(9, "2026-10-05T10:00:00")
        val afternoon = item(4, "2026-10-05T18:00:00")
        val items = listOf(morningLow, afternoon, morningHigh)

        assertEquals(
            listOf(4L, 9L, 2L),
            items.sortedWith(ordersListComparator(OrdersListSort.NEWEST_FIRST)).map { it.id },
        )
        assertEquals(
            listOf(9L, 2L, 4L),
            items.sortedWith(ordersListComparator(OrdersListSort.OLDEST_FIRST)).map { it.id },
        )

        val newestGroups = groupAgendaAppointments(items, today)
        assertEquals(listOf(4L, 9L, 2L), newestGroups.single().items.map { it.id })
        val oldestGroups = groupAgendaAppointments(items, today, sort = OrdersListSort.OLDEST_FIRST)
        assertEquals(listOf(9L, 2L, 4L), oldestGroups.single().items.map { it.id })
    }

    @Test
    fun `missing booking dates stay last then break ties by id descending`() {
        val dated = item(1, "2026-10-05T12:00:00")
        val undatedLow = item(3, null)
        val undatedHigh = item(8, null)
        val items = listOf(undatedLow, dated, undatedHigh)

        assertEquals(
            listOf(1L, 8L, 3L),
            items.sortedWith(ordersListComparator(OrdersListSort.NEWEST_FIRST)).map { it.id },
        )
        assertEquals(
            listOf(1L, 8L, 3L),
            items.sortedWith(ordersListComparator(OrdersListSort.OLDEST_FIRST)).map { it.id },
        )
    }

    @Test
    fun `arrangedFor uses the selected sort on each tab`() {
        val items = listOf(
            item(5, "2026-09-25T10:00:00", BookingStatus.CONFIRMED),
            item(6, "2026-09-28T14:00:00", BookingStatus.PENDING, ServiceRequestStatus.PENDING),
            item(11, "2026-09-29T10:00:00", BookingStatus.IN_PROGRESS),
            item(15, "2026-10-05T15:00:00", BookingStatus.EN_ROUTE),
            item(17, "2026-09-21T10:00:00", BookingStatus.CANCELLED, ServiceRequestStatus.CANCELLED),
            item(18, "2026-09-20T10:00:00", BookingStatus.FINISHED),
        )
        assertEquals(
            listOf(15L, 11L, 6L, 5L),
            items.arrangedFor(OrdersListFilter.ACTIVE, now).map { it.id },
        )
        assertEquals(
            listOf(5L, 6L, 11L, 15L),
            items.arrangedFor(OrdersListFilter.ACTIVE, now, OrdersListSort.OLDEST_FIRST).map { it.id },
        )
        assertEquals(
            listOf(17L, 18L),
            items.arrangedFor(OrdersListFilter.HISTORY, now).map { it.id },
        )
        assertEquals(
            listOf(18L, 17L),
            items.arrangedFor(OrdersListFilter.HISTORY, now, OrdersListSort.OLDEST_FIRST).map { it.id },
        )
    }

    @Test
    fun `sort prefs are independent per tab`() {
        val prefs = OrdersListSortPrefs()
            .with(OrdersListFilter.ACTIVE, OrdersListSort.OLDEST_FIRST)
        assertEquals(OrdersListSort.OLDEST_FIRST, prefs.forFilter(OrdersListFilter.ACTIVE))
        assertEquals(OrdersListSort.NEWEST_FIRST, prefs.forFilter(OrdersListFilter.HISTORY))

        val both = prefs.with(OrdersListFilter.HISTORY, OrdersListSort.OLDEST_FIRST)
        assertEquals(OrdersListSort.OLDEST_FIRST, both.forFilter(OrdersListFilter.ACTIVE))
        assertEquals(OrdersListSort.OLDEST_FIRST, both.forFilter(OrdersListFilter.HISTORY))
    }

    private fun item(
        id: Long,
        sortKey: String?,
        booking: BookingStatus? = BookingStatus.CONFIRMED,
        status: ServiceRequestStatus = ServiceRequestStatus.ACCEPTED,
    ) = ServiceOrderListItem(
        id = id,
        counterpartName = "Ana",
        subRubroLabel = "Plomería",
        status = status,
        bookingStatus = booking,
        scheduledLabel = null,
        addressLabel = "Calle 1",
        rubroLabel = "Plomería",
        scheduledSortKey = sortKey,
    )
}
