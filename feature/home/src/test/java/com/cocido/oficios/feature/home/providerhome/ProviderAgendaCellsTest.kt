package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.groupAgendaAppointments
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * La home de trabajador ya no dibuja un mini-calendario (Figma 259:254).
 * Este test cubre el agrupado por fecha que alimenta la lista.
 */
class ProviderAgendaCellsTest {

    @Test
    fun `la lista agrupa por dia y marca HOY`() {
        val today = LocalDate.of(2026, 3, 4)
        val groups = groupAgendaAppointments(
            orders = listOf(
                item(1, "2026-03-04T13:30:00"),
                item(2, "2026-03-04T18:00:00"),
                item(3, "2026-03-06T09:00:00"),
            ),
            today = today,
        )
        assertEquals(2, groups.size)
        assertEquals(2, groups[0].items.size)
        assertEquals(true, groups[0].isToday)
        assertEquals(false, groups[1].isToday)
        assertEquals("4 de marzo", groups[0].headerLabel)
    }

    private fun item(id: Long, sortKey: String) = ServiceOrderListItem(
        id = id,
        counterpartName = "Cliente",
        subRubroLabel = "Albañilería",
        status = ServiceRequestStatus.ACCEPTED,
        bookingStatus = BookingStatus.CONFIRMED,
        scheduledLabel = null,
        addressLabel = "Maipú 745",
        rubroLabel = "Albañilería",
        scheduledSortKey = sortKey,
    )
}
