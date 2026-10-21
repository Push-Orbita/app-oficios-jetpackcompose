package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal object CreateOrderSchedule {
    private val DAY_KEYS = mapOf(
        DayOfWeek.MONDAY to "LUNES",
        DayOfWeek.TUESDAY to "MARTES",
        DayOfWeek.WEDNESDAY to "MIERCOLES",
        DayOfWeek.THURSDAY to "JUEVES",
        DayOfWeek.FRIDAY to "VIERNES",
        DayOfWeek.SATURDAY to "SABADO",
        DayOfWeek.SUNDAY to "DOMINGO",
    )

    fun spanishDayKey(date: LocalDate): String = DAY_KEYS.getValue(date.dayOfWeek)

    fun isWorkDay(date: LocalDate, workDayKeys: Set<String>): Boolean {
        if (workDayKeys.isEmpty()) return true
        return spanishDayKey(date) in workDayKeys
    }

    fun isDateEnabled(
        date: LocalDate,
        today: LocalDate,
        workDayKeys: Set<String>,
    ): Boolean = !date.isBefore(today) && isWorkDay(date, workDayKeys)

    fun matchingSlot(
        slots: List<AgendaSlot>,
        date: LocalDate?,
        hour: LocalTime?,
    ): AgendaSlot? {
        if (date == null || hour == null) return null
        return slots.firstOrNull { slot ->
            slot.status == AgendaSlotStatus.DISPONIBLE &&
                slot.start.toLocalDate() == date &&
                slot.start.toLocalTime().hour == hour.hour
        }
    }

    fun availableHoursForDate(slots: List<AgendaSlot>, date: LocalDate?): Set<Int> {
        if (date == null) return emptySet()
        return slots
            .filter { it.status == AgendaSlotStatus.DISPONIBLE && it.start.toLocalDate() == date }
            .map { it.start.hour }
            .toSet()
    }

    fun visitDateTime(date: LocalDate?, hour: LocalTime?): LocalDateTime? {
        if (date == null || hour == null) return null
        return date.atTime(hour)
    }

    fun canContinue(
        selectedSubRubroId: Long?,
        selectedDomicilioId: Long?,
        selectedContactoId: Long?,
        selectedDate: LocalDate?,
        selectedHour: LocalTime?,
    ): Boolean = selectedSubRubroId != null &&
        selectedDomicilioId != null &&
        selectedContactoId != null &&
        selectedDate != null &&
        selectedHour != null
}
