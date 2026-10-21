package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentClientWorkersTest {

    @Test
    fun `dedupes workers and keeps the newest request first`() {
        val orders = listOf(
            order(id = 1, workerId = 10, name = "Ana Vieja", sortKey = "2026-01-01T10:00:00"),
            order(id = 3, workerId = 10, name = "Ana Nueva", sortKey = "2026-09-30T10:00:00"),
            order(id = 2, workerId = 20, name = "Carlos", sortKey = "2026-09-01T10:00:00"),
            order(id = 4, workerId = 0, name = "Sin trabajador", sortKey = "2026-10-01T10:00:00"),
        )

        val recents = orders.recentClientWorkers()

        assertEquals(listOf(10L, 20L), recents.map { it.id })
        assertEquals("Ana Nueva", recents.first().displayName)
        assertEquals("Plomería", recents.first().specialtyLabel)
        assertEquals(true, recents.first().isVerified)
    }

    @Test
    fun `hides empty counterpart and respects limit`() {
        val orders = (1L..5L).map { index ->
            order(id = index, workerId = index, name = "Trabajador $index", sortKey = "2026-09-0$index")
        } + order(id = 9, workerId = 99, name = "  ", sortKey = "2026-10-01")

        val recents = orders.recentClientWorkers(limit = 3)

        assertEquals(3, recents.size)
        assertEquals(listOf(5L, 4L, 3L), recents.map { it.id })
    }

    private fun order(
        id: Long,
        workerId: Long,
        name: String,
        sortKey: String?,
    ) = ServiceOrderListItem(
        id = id,
        counterpartName = name,
        subRubroLabel = "Cañerías",
        status = ServiceRequestStatus.ACCEPTED,
        bookingStatus = BookingStatus.CONFIRMED,
        scheduledLabel = null,
        addressLabel = "Av. 25 de Mayo 450",
        rubroLabel = "Plomería",
        avatarUrl = "https://cdn.example/a.jpg",
        isVerified = true,
        workerId = workerId,
        scheduledSortKey = sortKey,
    )
}
