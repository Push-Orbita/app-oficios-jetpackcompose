package com.cocido.oficios.domain.model

data class WorkHourRange(
    val startHour: Int,
    val endHour: Int,
) {
    val startLabel: String get() = "%02d:00".format(startHour)
    val endLabel: String get() = "%02d:00".format(endHour)
}

data class WorkDaySchedule(
    val dayKey: String,
    val hours: Set<Int>,
) {
    fun ranges(): List<WorkHourRange> = mergeHours(hours)
}

object WorkHoursSchedule {
    val WEEK_DAYS = listOf("DOMINGO", "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO")
    const val MAX_HOURS_PER_DAY = 12

    fun canAddHour(current: Set<Int>, hour: Int): Boolean {
        if (hour in current) return true
        return current.size < MAX_HOURS_PER_DAY
    }

    fun toggleHour(current: Set<Int>, hour: Int): Set<Int> {
        return if (hour in current) current - hour
        else if (current.size >= MAX_HOURS_PER_DAY) current
        else current + hour
    }

    fun toDiaLaboralPayload(selection: Map<String, Set<Int>>): List<WorkDaySchedule> =
        WEEK_DAYS.mapNotNull { day ->
            val hours = selection[day].orEmpty()
            if (hours.isEmpty()) null else WorkDaySchedule(day, hours)
        }

    fun workDayKeys(selection: Map<String, Set<Int>>): List<String> =
        toDiaLaboralPayload(selection).map { it.dayKey }

    fun timeSlotsFor(hours: Set<Int>): List<RegisterTradeTimeSlotDraft> =
        mergeHours(hours).map { RegisterTradeTimeSlotDraft(from = it.startLabel, to = it.endLabel) }

    fun expandRanges(ranges: List<WorkHourRange>): Set<Int> =
        ranges.flatMap { (it.startHour until it.endHour).toList() }.toSet()
}

internal fun mergeHours(hours: Set<Int>): List<WorkHourRange> {
    if (hours.isEmpty()) return emptyList()
    val sorted = hours.sorted()
    val ranges = mutableListOf<WorkHourRange>()
    var start = sorted.first()
    var end = start + 1
    for (hour in sorted.drop(1)) {
        if (hour == end) {
            end = hour + 1
        } else {
            ranges += WorkHourRange(start, end)
            start = hour
            end = hour + 1
        }
    }
    ranges += WorkHourRange(start, end)
    return ranges
}
