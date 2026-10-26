package com.cocido.oficios.domain.model

data class AvailabilityPut(
    val subRubroId: Long,
    val week: WeeklyAvailability,
)

fun availabilityPutsForEdit(
    persistedSubIds: List<Long>,
    originalBySubId: Map<Long, WeeklyAvailability>,
    editorSourceSubId: Long?,
    wizardWeek: WeeklyAvailability,
    draftBySubId: Map<Long, WeeklyAvailability> = emptyMap(),
): List<AvailabilityPut> = persistedSubIds.filter { it > 0L }.mapNotNull { subId ->
    val original = originalBySubId[subId]
    val draft = if (subId == editorSourceSubId) wizardWeek else draftBySubId[subId]
    when {
        original == null -> AvailabilityPut(subId, draft ?: wizardWeek)
        draft != null && draft != original -> AvailabilityPut(subId, draft)
        else -> null
    }
}

fun WeeklyAvailability.toManagedSchedules(): List<ManagedWorkSchedule> =
    days.filter { it.ranges.isNotEmpty() }.map { day ->
        ManagedWorkSchedule(
            dayFrom = day.day,
            dayTo = day.day,
            ranges = day.ranges.map { ManagedTimeRange(from = it.start, to = it.end) },
        )
    }
