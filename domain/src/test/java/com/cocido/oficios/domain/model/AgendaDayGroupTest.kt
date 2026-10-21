package com.cocido.oficios.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaDayGroupTest {

    private val today = LocalDate.of(2026, 3, 4)

    @Test
    fun `groups by date, lowercase month, HOY on today`() {
        val groups = groupAgendaAppointments(
            orders = listOf(
                order(1, "Albañilería", "2026-03-04T13:30:00", "Juan José Silva 3800"),
                order(2, "Albañilería", "2026-03-06T18:30:00", "Av. Nestor Kirchner 6435"),
                order(3, "Plomeria", "2026-03-17T09:30:00", "Maipú 745"),
            ),
            today = today,
        )
        assertEquals(3, groups.size)
        assertEquals("4 de marzo", groups[0].headerLabel)
        assertTrue(groups[0].isToday)
        assertEquals("6 de marzo", groups[1].headerLabel)
        assertFalse(groups[1].isToday)
        assertEquals("17 de marzo", groups[2].headerLabel)
        assertEquals("13:30hs", groups[0].items[0].agendaTimeLabel())
        assertEquals("Albañilería", groups[0].items[0].agendaRubroLabel())
    }

    @Test
    fun `newestFirst puts recent days first`() {
        val groups = groupAgendaAppointments(
            orders = listOf(
                order(1, "Albañilería", "2026-03-04T13:30:00"),
                order(2, "Albañilería", "2026-03-17T09:30:00"),
            ),
            today = today,
            newestFirst = true,
        )
        assertEquals(listOf("17 de marzo", "4 de marzo"), groups.map { it.headerLabel })
    }

    @Test
    fun `skips items without a scheduled date`() {
        val groups = groupAgendaAppointments(
            orders = listOf(order(1, "Albañilería", scheduledSortKey = null)),
            today = today,
        )
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `uses subRubro when rubro is blank`() {
        val item = order(1, rubro = "", scheduledSortKey = "2026-03-04T10:00:00").copy(
            subRubroLabel = "Pintura",
        )
        assertEquals("Pintura", item.agendaRubroLabel())
    }

    private fun order(
        id: Long,
        rubro: String,
        scheduledSortKey: String?,
        address: String = "",
    ) = ServiceOrderListItem(
        id = id,
        counterpartName = "Cliente",
        subRubroLabel = rubro,
        status = ServiceRequestStatus.ACCEPTED,
        bookingStatus = BookingStatus.CONFIRMED,
        scheduledLabel = null,
        addressLabel = address,
        rubroLabel = rubro,
        scheduledSortKey = scheduledSortKey,
    )
}
