package com.cocido.oficios.feature.home.agenda.availability

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleDayUi
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleRangeUi
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.withToggledDay
import com.cocido.oficios.feature.home.R

@Composable
internal fun weekDayLabels(): Map<String, String> = mapOf(
    "LUNES" to stringResource(R.string.agenda_day_monday),
    "MARTES" to stringResource(R.string.agenda_day_tuesday),
    "MIERCOLES" to stringResource(R.string.agenda_day_wednesday),
    "JUEVES" to stringResource(R.string.agenda_day_thursday),
    "VIERNES" to stringResource(R.string.agenda_day_friday),
    "SABADO" to stringResource(R.string.agenda_day_saturday),
    "DOMINGO" to stringResource(R.string.agenda_day_sunday),
)

internal fun WeeklyAvailability.toEditorDays(labels: Map<String, String>): List<WeeklyScheduleDayUi> {
    val keys = if (includeSunday) WeeklyAvailability.ALL_DAYS else WeeklyAvailability.WEEKDAYS
    return keys.map { key ->
        val ranges = rangesOn(key)
        WeeklyScheduleDayUi(
            key = key,
            label = labels[key] ?: key,
            enabled = ranges.isNotEmpty(),
            ranges = ranges.map { WeeklyScheduleRangeUi(it.start, it.end) },
        )
    }
}

internal fun WeeklyAvailability.toggleDay(day: String, enabled: Boolean): WeeklyAvailability =
    withToggledDay(day, enabled)
