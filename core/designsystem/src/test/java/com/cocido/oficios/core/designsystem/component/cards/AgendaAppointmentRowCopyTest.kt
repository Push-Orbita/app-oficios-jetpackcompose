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
    fun metaDropsDuplicateNameAndJoinsRubroAddressTime() {
        assertEquals(
            "Plomería · Av. 25 de Mayo 450 · 12:00hs",
            agendaRowMeta(
                rubro = "Plomería",
                address = "Av. 25 de Mayo 450",
                time = "12:00hs",
                title = "Ana Paz",
                counterpartName = "Ana Paz",
            ),
        )
        assertEquals(
            "12:00hs",
            agendaRowMeta(
                rubro = "Plomería",
                address = "Ana Paz",
                time = "12:00hs",
                title = "Plomería",
                counterpartName = "Ana Paz",
            ),
        )
    }
}
