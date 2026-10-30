package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.TimeRange
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Franja en la que el trabajador ya está ocupado: un turno RESERVADO/BLOQUEADO de la
 * agenda, un pedido activo del cliente con ese trabajador o un horario que el backend
 * ya rechazó por solaparse. Fin exclusivo.
 */
data class BusyInterval(
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    fun overlaps(otherStart: LocalDateTime, otherEnd: LocalDateTime): Boolean =
        otherStart.isBefore(end) && otherEnd.isAfter(start)
}

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
            slot.isSelectable &&
                slot.start.toLocalDate() == date &&
                slot.start.toLocalTime() == hour
        }
    }

    fun availableHoursForDate(slots: List<AgendaSlot>, date: LocalDate?): List<LocalTime> {
        if (date == null) return emptyList()
        return slots
            .filter { it.isSelectable && it.start.toLocalDate() == date }
            .map { it.start.toLocalTime() }
            .distinct()
            .sorted()
    }

    fun blockedHoursForDate(slots: List<AgendaSlot>, date: LocalDate?): Set<Int> {
        if (date == null) return emptySet()
        return slots
            .filter { !it.isSelectable && it.start.toLocalDate() == date }
            .map { it.start.hour }
            .toSet()
    }

    /** Duración que se manda al backend cuando el cliente propone fecha y hora (sin slot). */
    val DEFAULT_VISIT_DURATION: Duration = Duration.ofHours(2)

    /** Turnos no reservables de la agenda (ocupados, bloqueados o sin id) como franjas ocupadas. */
    fun busyFromSlots(slots: List<AgendaSlot>): List<BusyInterval> = slots
        .filter { !it.isSelectable && it.end.isAfter(it.start) }
        .map { BusyInterval(it.start, it.end) }

    /**
     * Horas de inicio cuya visita completa ([visitDuration]) entra en alguna franja.
     * Si la franja es más corta que la visita, sólo vale su hora de inicio.
     */
    fun hoursFittingRanges(ranges: List<TimeRange>, visitDuration: Duration): List<Int> {
        val visitMinutes = visitDuration.toMinutes().toInt()
        val hours = sortedSetOf<Int>()
        for (range in ranges) {
            val start = TimeRangeValidation.parseMinutes(range.start) ?: continue
            val end = TimeRangeValidation.parseMinutes(range.end) ?: continue
            if (end <= start) continue
            val needed = minOf(visitMinutes, end - start)
            for (hour in 0..23) {
                val hourStart = hour * 60
                if (hourStart >= start && hourStart + needed <= end) hours += hour
            }
        }
        return hours.toList()
    }

    /**
     * Horarios que el cliente puede elegir. `null` = sin restricción (API vieja / sin agenda).
     * Si hay slots disponibles, devuelve esas horas exactas; si no, cae a franjas horarias.
     * En los dos casos descarta las horas que ya pasaron y las que se solapan con
     * [busy] o con turnos ocupados de [slots]: el cliente no tiene que poder elegir un
     * horario que el backend después rechaza por solapamiento.
     */
    fun selectableHours(
        slots: List<AgendaSlot>,
        date: LocalDate?,
        ranges: List<TimeRange>,
        dayHasAvailability: Boolean?,
        busy: List<BusyInterval> = emptyList(),
        now: LocalDateTime? = null,
        visitDuration: Duration = DEFAULT_VISIT_DURATION,
    ): List<LocalTime>? {
        if (date == null) return emptyList()
        val occupied = busyFromSlots(slots) + busy
        fun isFree(start: LocalDateTime, end: LocalDateTime): Boolean =
            (now == null || start.isAfter(now)) && occupied.none { it.overlaps(start, end) }

        val daySlots = slots.filter { it.isSelectable && it.start.toLocalDate() == date }
        if (daySlots.isNotEmpty()) {
            return daySlots
                .filter { isFree(it.start, it.end) }
                .map { it.start.toLocalTime() }
                .distinct()
                .sorted()
        }
        fun hourIsFree(hour: Int): Boolean {
            val start = date.atTime(hour, 0)
            return isFree(start, start.plus(visitDuration))
        }
        val fromRanges = hoursFittingRanges(ranges, visitDuration)
        if (fromRanges.isNotEmpty()) return fromRanges.filter(::hourIsFree).map { LocalTime.of(it, 0) }
        if (dayHasAvailability != null) return emptyList()
        val unrestricted = (0..23).filter(::hourIsFree)
        return if (unrestricted.size == 24) null else unrestricted.map { LocalTime.of(it, 0) }
    }

    fun isHourSelectable(
        hour: LocalTime?,
        date: LocalDate?,
        slots: List<AgendaSlot>,
        ranges: List<TimeRange>,
        dayHasAvailability: Boolean?,
        busy: List<BusyInterval> = emptyList(),
        now: LocalDateTime? = null,
    ): Boolean {
        if (hour == null || date == null) return false
        val allowed = selectableHours(slots, date, ranges, dayHasAvailability, busy, now) ?: return true
        return hour in allowed
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

/** Hora actual en Argentina: las horas de hoy que ya pasaron no se ofrecen. */
internal fun createOrderNow(): LocalDateTime = LocalDateTime.now(DisplayDateTime.ARGENTINA)

/** Horarios elegibles para el día seleccionado, con todo lo que sabe el estado. */
internal fun CreateOrderUiState.Success.selectableHours(
    date: LocalDate? = selectedDate,
    now: LocalDateTime = createOrderNow(),
): List<LocalTime>? = CreateOrderSchedule.selectableHours(
    slots = availableSlots,
    date = date,
    ranges = dayRanges,
    dayHasAvailability = dayHasAvailability,
    busy = busyIntervals,
    now = now,
)

internal fun CreateOrderUiState.Success.isHourSelectable(
    hour: LocalTime?,
    date: LocalDate? = selectedDate,
    now: LocalDateTime = createOrderNow(),
): Boolean = CreateOrderSchedule.isHourSelectable(
    hour = hour,
    date = date,
    slots = availableSlots,
    ranges = dayRanges,
    dayHasAvailability = dayHasAvailability,
    busy = busyIntervals,
    now = now,
)
