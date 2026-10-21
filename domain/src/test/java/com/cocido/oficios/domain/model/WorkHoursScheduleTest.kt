package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkHoursScheduleTest {

    @Test
    fun `merge consecutive hours into ranges`() {
        val ranges = WorkHoursSchedule.timeSlotsFor(setOf(8, 9, 10, 14))
        assertEquals(
            listOf(
                RegisterTradeTimeSlotDraft("08:00", "11:00"),
                RegisterTradeTimeSlotDraft("14:00", "15:00"),
            ),
            ranges,
        )
    }

    @Test
    fun `payload uses one diaInicio equals diaFin per day`() {
        val payload = WorkHoursSchedule.toDiaLaboralPayload(
            mapOf(
                "LUNES" to setOf(9, 10),
                "MIERCOLES" to setOf(16),
                "DOMINGO" to emptySet(),
            ),
        )
        assertEquals(listOf("LUNES", "MIERCOLES"), payload.map { it.dayKey })
        assertEquals(listOf(WorkHourRange(9, 11)), payload.first().ranges())
    }

    @Test
    fun `caps twelve hours per day`() {
        val twelve = (8..19).toSet()
        assertFalse(WorkHoursSchedule.canAddHour(twelve, 20))
        assertEquals(twelve, WorkHoursSchedule.toggleHour(twelve, 20))
        assertEquals(eleven(twelve), WorkHoursSchedule.toggleHour(twelve, 8))
    }

    @Test
    fun `expand ranges back to hours`() {
        val hours = WorkHoursSchedule.expandRanges(listOf(WorkHourRange(9, 12), WorkHourRange(16, 17)))
        assertEquals(setOf(9, 10, 11, 16), hours)
        assertTrue(WorkHoursSchedule.canAddHour(emptySet(), 8))
    }

    private fun eleven(hours: Set<Int>) = hours - 8
}
