package com.cocido.oficios.domain.model

/** Agenda semanal editable, con franjas distintas por día. */
typealias WeeklyScheduleDraft = WeeklyAvailability

fun weeklyScheduleFromFlat(
    workDays: Collection<String>,
    timeSlots: List<RegisterTradeTimeSlotDraft>,
): WeeklyScheduleDraft {
    val ranges = timeSlots
        .filter { it.from.isNotBlank() && it.to.isNotBlank() }
        .map { TimeRange(TimeRangeValidation.normalize(it.from), TimeRangeValidation.normalize(it.to)) }
        .ifEmpty { listOf(TimeRange("09:00", "18:00")) }
    val selected = workDays.map { it.uppercase() }
        .filter { it in WeeklyAvailability.ALL_DAYS }
        .ifEmpty { WeeklyAvailability.DEFAULT_DAYS }
    return WeeklyAvailability(
        days = selected.distinct().map { DayAvailability(it, ranges) },
        includeSunday = WeeklyAvailability.SUNDAY in selected,
    )
}

fun WeeklyAvailability.toFlatTimeSlots(): List<RegisterTradeTimeSlotDraft> =
    days.flatMap { day -> day.ranges.map { RegisterTradeTimeSlotDraft(it.start, it.end) } }
        .distinctBy { "${it.from}-${it.to}" }
        .ifEmpty { listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")) }

fun RegisterTradeDraft.resolvedSchedule(): WeeklyScheduleDraft =
    weeklySchedule ?: weeklyScheduleFromFlat(workDays, timeSlots)

fun WeeklyAvailability.copyMondayToWeekdays(): WeeklyAvailability {
    val monday = rangesOn("LUNES")
    if (monday.isEmpty()) return this
    var next = this
    for (day in listOf("MARTES", "MIERCOLES", "JUEVES", "VIERNES")) {
        next = next.withRange(day, monday)
    }
    return next
}

fun WeeklyAvailability.insertRange(day: String, index: Int, range: TimeRange): WeeklyAvailability {
    val current = rangesOn(day).toMutableList()
    current.add(index.coerceIn(0, current.size), range)
    return withRange(day, current)
}
