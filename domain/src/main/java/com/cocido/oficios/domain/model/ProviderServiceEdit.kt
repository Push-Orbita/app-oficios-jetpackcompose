package com.cocido.oficios.domain.model

data class ProviderSubRubroEditPlan(
    val toCreateCatalogIds: List<Long>,
    val toDeleteIds: List<Long>,
    val toKeep: List<ManagedProviderSubService>,
)

fun planSubRubroEdits(
    existing: List<ManagedProviderSubService>,
    requestedCatalogIds: Collection<Long>,
): ProviderSubRubroEditPlan {
    val requested = requestedCatalogIds.filter { it > 0L }.distinct()
    val existingByCatalog = existing.filter { it.catalogSubRubroId > 0L }.associateBy { it.catalogSubRubroId }
    return ProviderSubRubroEditPlan(
        toCreateCatalogIds = requested.filter { it !in existingByCatalog },
        toDeleteIds = existing
            .filter { it.catalogSubRubroId !in requested }
            .map { it.id }
            .filter { it > 0L },
        toKeep = requested.mapNotNull { existingByCatalog[it] },
    )
}

data class AvailabilityPut(
    val subRubroId: Long,
    val week: WeeklyAvailability,
)

fun availabilityPutsForEdit(
    persistedSubIds: List<Long>,
    originalBySubId: Map<Long, WeeklyAvailability>,
    editorSourceSubId: Long?,
    wizardWeek: WeeklyAvailability,
): List<AvailabilityPut> = persistedSubIds.filter { it > 0L }.mapNotNull { subId ->
    val original = originalBySubId[subId]
    when {
        original == null -> AvailabilityPut(subId, wizardWeek)
        subId == editorSourceSubId && original != wizardWeek -> AvailabilityPut(subId, wizardWeek)
        else -> null
    }
}

fun List<WeeklyAvailability>.mergedForDisplay(): WeeklyAvailability {
    if (isEmpty()) return WeeklyAvailability.default()
    val byDay = linkedMapOf<String, MutableList<TimeRange>>()
    for (week in this) {
        for (day in week.days) {
            if (day.ranges.isEmpty()) continue
            byDay.getOrPut(day.day) { mutableListOf() } += day.ranges
        }
    }
    val days = WeeklyAvailability.ALL_DAYS.mapNotNull { key ->
        val ranges = byDay[key]?.distinct() ?: return@mapNotNull null
        if (ranges.isEmpty()) null else DayAvailability(key, ranges)
    }
    return if (days.isEmpty()) {
        WeeklyAvailability.default()
    } else {
        WeeklyAvailability(days = days, includeSunday = days.any { it.day == WeeklyAvailability.SUNDAY })
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
