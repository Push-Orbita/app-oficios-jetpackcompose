package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ManagedWorkScheduleTest {

    @Test
    fun `expandedDays includes inclusive weekday range`() {
        val schedule = ManagedWorkSchedule(
            dayFrom = "LUNES",
            dayTo = "VIERNES",
            ranges = listOf(ManagedTimeRange("09:00", "18:00")),
        )
        assertEquals(
            listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"),
            schedule.expandedDays(),
        )
    }

    @Test
    fun `expandedDays keeps a single day`() {
        val schedule = ManagedWorkSchedule(
            dayFrom = "SABADO",
            dayTo = "SABADO",
            ranges = emptyList(),
        )
        assertEquals(listOf("SABADO"), schedule.expandedDays())
    }
}
