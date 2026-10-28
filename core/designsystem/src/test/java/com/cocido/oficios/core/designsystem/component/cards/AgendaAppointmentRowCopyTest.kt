package com.cocido.oficios.core.designsystem.component.cards

import org.junit.Assert.assertEquals
import org.junit.Test

class AgendaAppointmentRowCopyTest {

    @Test
    fun titlePrefersCounterpartName() {
        assertEquals("Ana Paz", agendaRowTitle(rubro = "Plomería", counterpartName = "Ana Paz"))
        assertEquals("Plomería", agendaRowTitle(rubro = "Plomería", counterpartName = "  "))
        assertEquals("Plomería", agendaRowTitle(rubro = "Plomería", counterpartName = null))
    }

    @Test
    fun detailJoinsRubroAndAddressWithoutTime() {
        assertEquals(
            "Plomería · Av. 25 de Mayo 450",
            agendaRowDetail(
                rubro = "Plomería",
                address = "Av. 25 de Mayo 450",
                title = "Ana Paz",
                counterpartName = "Ana Paz",
            ),
        )
        assertEquals(
            "",
            agendaRowDetail(
                rubro = "Plomería",
                address = "Ana Paz",
                title = "Plomería",
                counterpartName = "Ana Paz",
            ),
        )
    }
}
