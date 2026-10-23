package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateOrderScheduleTest {

    @Test
    fun `sunday maps to DOMINGO`() {
        assertEquals("DOMINGO", CreateOrderSchedule.spanishDayKey(LocalDate.of(2026, 3, 1)))
        assertEquals("LUNES", CreateOrderSchedule.spanishDayKey(LocalDate.of(2026, 3, 2)))
    }

    @Test
    fun `empty work days leaves every day enabled except past`() {
        val today = LocalDate.of(2026, 3, 4)
        assertFalse(CreateOrderSchedule.isDateEnabled(today.minusDays(1), today, emptySet()))
        assertTrue(CreateOrderSchedule.isDateEnabled(today, today, emptySet()))
        assertTrue(CreateOrderSchedule.isDateEnabled(today.plusDays(3), today, emptySet()))
    }

    @Test
    fun `work days disable other weekdays`() {
        val today = LocalDate.of(2026, 3, 4) // Wednesday
        val work = setOf("LUNES", "MIERCOLES", "VIERNES")
        assertTrue(CreateOrderSchedule.isDateEnabled(today, today, work))
        assertFalse(CreateOrderSchedule.isDateEnabled(LocalDate.of(2026, 3, 5), today, work))
        assertTrue(CreateOrderSchedule.isDateEnabled(LocalDate.of(2026, 3, 6), today, work))
    }

    @Test
    fun `matching slot uses hour of the selected day`() {
        val date = LocalDate.of(2026, 3, 4)
        val slots = listOf(
            slot(id = 9, start = date.atTime(10, 0)),
            slot(id = 11, start = date.plusDays(1).atTime(10, 0)),
        )
        val match = CreateOrderSchedule.matchingSlot(slots, date, LocalTime.of(10, 0))
        assertEquals(9L, match?.id)
        assertNull(CreateOrderSchedule.matchingSlot(slots, date, LocalTime.of(11, 0)))
    }

    @Test
    fun `slots with id zero or blocked are not selectable`() {
        val date = LocalDate.of(2026, 3, 4)
        val slots = listOf(
            slot(id = 0, start = date.atTime(10, 0), status = AgendaSlotStatus.BLOQUEADO),
            slot(id = 0, start = date.atTime(11, 0)),
            slot(id = 4, start = date.atTime(12, 0)),
        )
        assertNull(CreateOrderSchedule.matchingSlot(slots, date, LocalTime.of(10, 0)))
        assertNull(CreateOrderSchedule.matchingSlot(slots, date, LocalTime.of(11, 0)))
        assertEquals(4L, CreateOrderSchedule.matchingSlot(slots, date, LocalTime.of(12, 0))?.id)
        assertEquals(setOf(12), CreateOrderSchedule.availableHoursForDate(slots, date))
        assertEquals(setOf(10, 11), CreateOrderSchedule.blockedHoursForDate(slots, date))
        val selectable = CreateOrderSchedule.selectableHours(
            slots = slots,
            date = date,
            ranges = emptyList(),
            dayHasAvailability = true,
        )
        assertEquals(setOf(12), selectable)
        assertFalse(
            CreateOrderSchedule.isHourSelectable(
                hour = LocalTime.of(10, 0),
                date = date,
                slots = slots,
                ranges = emptyList(),
                dayHasAvailability = true,
            ),
        )
    }

    @Test
    fun `canContinue requires domicilio contacto specialty and datetime`() {
        assertFalse(
            CreateOrderSchedule.canContinue(
                selectedSubRubroId = 1,
                selectedDomicilioId = 2,
                selectedContactoId = 3,
                selectedDate = null,
                selectedHour = LocalTime.of(10, 0),
            ),
        )
        assertTrue(
            CreateOrderSchedule.canContinue(
                selectedSubRubroId = 1,
                selectedDomicilioId = 2,
                selectedContactoId = 3,
                selectedDate = LocalDate.of(2026, 3, 4),
                selectedHour = LocalTime.of(10, 0),
            ),
        )
    }

    private fun slot(
        id: Long,
        start: LocalDateTime,
        status: AgendaSlotStatus = AgendaSlotStatus.DISPONIBLE,
    ) = AgendaSlot(
        id = id,
        subRubroId = 1,
        subRubroLabel = "Albañilería",
        start = start,
        end = start.plusHours(1),
        status = status,
        autoAcceptance = false,
    )
}
