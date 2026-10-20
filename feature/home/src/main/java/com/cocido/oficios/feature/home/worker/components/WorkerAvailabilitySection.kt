package com.cocido.oficios.feature.home.worker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.WorkerAvailability
import com.cocido.oficios.domain.model.WorkerWorkDay
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerAvailabilitySection(
    availability: WorkerAvailability?,
    modifier: Modifier = Modifier,
) {
    if (availability == null || availability.workDays.isEmpty()) return

    WorkerProfileSection(
        title = stringResource(R.string.home_worker_availability_title),
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            availability.workDays.forEach { day ->
                AvailabilityDay(day)
            }
        }
    }
}

@Composable
private fun AvailabilityDay(day: WorkerWorkDay) {
    val dayLabel = if (day.dayStart.equals(day.dayEnd, ignoreCase = true)) {
        day.dayStart.dayName()
    } else {
        stringResource(R.string.home_worker_day_range, day.dayStart.dayName(), day.dayEnd.dayName())
    }
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        day.hours.forEach { range ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = stringResource(R.string.home_worker_hours_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(OficiosComponentSize.iconSm),
                )
                Text(
                    text = stringResource(R.string.home_worker_hours_range, range.startTime, range.endTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun String.dayName(): String = when (uppercase()) {
    "LUNES" -> stringResource(R.string.home_worker_day_monday)
    "MARTES" -> stringResource(R.string.home_worker_day_tuesday)
    "MIERCOLES", "MIÉRCOLES" -> stringResource(R.string.home_worker_day_wednesday)
    "JUEVES" -> stringResource(R.string.home_worker_day_thursday)
    "VIERNES" -> stringResource(R.string.home_worker_day_friday)
    "SABADO", "SÁBADO" -> stringResource(R.string.home_worker_day_saturday)
    "DOMINGO" -> stringResource(R.string.home_worker_day_sunday)
    else -> this
}
