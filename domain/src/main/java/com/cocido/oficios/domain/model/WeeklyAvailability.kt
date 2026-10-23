package com.cocido.oficios.domain.model

data class TimeRange(
    val start: String,
    val end: String,
)

data class DayAvailability(
    val day: String,
    val ranges: List<TimeRange>,
)

data class WeeklyAvailability(
    val days: List<DayAvailability> = emptyList(),
    val includeSunday: Boolean = false,
) {
    val activeDays: List<String>
        get() = days.filter { it.ranges.isNotEmpty() }.map { it.day }

    fun rangesOn(day: String): List<TimeRange> =
        days.firstOrNull { it.day == day }?.ranges.orEmpty()

    companion object {
        val WEEKDAYS = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO")
        val DEFAULT_DAYS = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")
        const val SUNDAY = "DOMINGO"
        val ALL_DAYS = WEEKDAYS + SUNDAY

        fun default(): WeeklyAvailability = WeeklyAvailability(
            days = DEFAULT_DAYS.map { day ->
                DayAvailability(day, listOf(TimeRange("09:00", "18:00")))
            },
            includeSunday = false,
        )
    }
}

data class AgendaBlock(
    val id: Long,
    val date: String,
    val start: String? = null,
    val end: String? = null,
    val reason: String? = null,
) {
    val isFullDay: Boolean get() = start.isNullOrBlank() && end.isNullOrBlank()
}

data class CreateAgendaBlockParams(
    val subRubroId: Long,
    val date: String,
    val start: String? = null,
    val end: String? = null,
    val reason: String? = null,
)

object TimeRangeValidation {
    private val HhMm = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun normalize(raw: String): String {
        val match = Regex("""(\d{1,2}):(\d{2})""").find(raw.trim()) ?: return raw.trim()
        return "${match.groupValues[1].padStart(2, '0')}:${match.groupValues[2]}"
    }

    fun parseMinutes(value: String): Int? {
        val normalized = normalize(value)
        if (!normalized.matches(HhMm)) return null
        val hours = normalized.substring(0, 2).toInt()
        val minutes = normalized.substring(3, 5).toInt()
        return hours * 60 + minutes
    }

    fun validateRange(range: TimeRange): String? {
        val start = parseMinutes(range.start) ?: return "Use el formato HH:mm (por ejemplo 08:00)."
        val end = parseMinutes(range.end) ?: return "Use el formato HH:mm (por ejemplo 12:00)."
        if (start >= end) return "La hora de fin debe ser posterior al inicio."
        return null
    }

    fun validateRanges(ranges: List<TimeRange>): String? {
        if (ranges.isEmpty()) return null
        ranges.forEach { range -> validateRange(range)?.let { return it } }
        val sorted = ranges.map { parseMinutes(it.start)!! to parseMinutes(it.end)!! }.sortedBy { it.first }
        sorted.zipWithNext().forEach { (previous, next) ->
            if (previous.second > next.first) return "Las franjas de un mismo día no pueden superponerse."
        }
        return null
    }

    fun validateWeek(week: WeeklyAvailability): String? {
        val visibleDays = if (week.includeSunday) WeeklyAvailability.ALL_DAYS else WeeklyAvailability.WEEKDAYS
        week.days.filter { it.day in visibleDays }.forEach { day ->
            validateRanges(day.ranges)?.let { return it }
        }
        return null
    }

    fun hoursCovered(ranges: List<TimeRange>): Set<Int> {
        val hours = mutableSetOf<Int>()
        for (range in ranges) {
            val start = parseMinutes(range.start) ?: continue
            val end = parseMinutes(range.end) ?: continue
            for (hour in 0..23) {
                val hourStart = hour * 60
                if (hourStart >= start && hourStart < end) hours += hour
            }
        }
        return hours
    }

    fun validateBlock(date: String, start: String?, end: String?): String? {
        if (!date.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
            return "Indique la fecha en formato AAAA-MM-DD."
        }
        val hasStart = !start.isNullOrBlank()
        val hasEnd = !end.isNullOrBlank()
        if (hasStart != hasEnd) return "Indique inicio y fin, o deje ambas vacías para bloquear el día entero."
        if (hasStart && hasEnd) return validateRange(TimeRange(start!!, end!!))
        return null
    }
}

fun WeeklyAvailability.withToggledDay(day: String, enabled: Boolean): WeeklyAvailability {
    val remaining = days.filterNot { it.day == day }
    val nextDays = if (enabled) {
        remaining + DayAvailability(day, listOf(TimeRange("09:00", "18:00")))
    } else {
        remaining
    }
    return copy(days = nextDays.sortedBy { WeeklyAvailability.ALL_DAYS.indexOf(it.day) })
}

fun WeeklyAvailability.withRange(day: String, ranges: List<TimeRange>): WeeklyAvailability {
    val remaining = days.filterNot { it.day == day }
    val nextDays = if (ranges.isEmpty()) remaining else remaining + DayAvailability(day, ranges)
    return copy(days = nextDays.sortedBy { WeeklyAvailability.ALL_DAYS.indexOf(it.day) })
}

fun WeeklyAvailability.changeRange(
    day: String,
    index: Int,
    from: String? = null,
    to: String? = null,
): WeeklyAvailability {
    val current = rangesOn(day).toMutableList()
    if (index !in current.indices) return this
    val range = current[index]
    current[index] = TimeRange(start = from ?: range.start, end = to ?: range.end)
    return withRange(day, current)
}

fun WeeklyAvailability.addRange(day: String): WeeklyAvailability {
    val current = rangesOn(day).ifEmpty { listOf(TimeRange("09:00", "18:00")) }
    return withRange(day, current + TimeRange("14:00", "18:00"))
}

fun WeeklyAvailability.removeRange(day: String, index: Int): WeeklyAvailability {
    val current = rangesOn(day).toMutableList()
    if (index in current.indices) current.removeAt(index)
    return withRange(day, current.ifEmpty { listOf(TimeRange("09:00", "18:00")) })
}

fun WeeklyAvailability.toReplaceDays(): List<DayAvailability> {
    val allowed = if (includeSunday) WeeklyAvailability.ALL_DAYS else WeeklyAvailability.WEEKDAYS
    return days.filter { it.day in allowed && it.ranges.isNotEmpty() }
}

fun List<ManagedWorkSchedule>.toWeeklyAvailability(): WeeklyAvailability {
    val byDay = linkedMapOf<String, MutableList<TimeRange>>()
    for (schedule in this) {
        for (day in schedule.expandedDays()) {
            byDay.getOrPut(day) { mutableListOf() } += schedule.ranges.map { range ->
                TimeRange(TimeRangeValidation.normalize(range.from), TimeRangeValidation.normalize(range.to))
            }
        }
    }
    val days = WeeklyAvailability.ALL_DAYS.mapNotNull { day ->
        val ranges = byDay[day]?.distinct() ?: return@mapNotNull null
        if (ranges.isEmpty()) null else DayAvailability(day, ranges)
    }
    return WeeklyAvailability(
        days = days,
        includeSunday = days.any { it.day == WeeklyAvailability.SUNDAY },
    ).let { if (it.days.isEmpty()) WeeklyAvailability.default() else it }
}
