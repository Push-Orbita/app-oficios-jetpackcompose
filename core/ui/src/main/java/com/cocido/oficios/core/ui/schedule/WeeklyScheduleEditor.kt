package com.cocido.oficios.core.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.R
import kotlinx.coroutines.launch

data class WeeklyScheduleDayUi(
    val key: String,
    val label: String,
    val enabled: Boolean,
    val ranges: List<WeeklyScheduleRangeUi>,
)

data class WeeklyScheduleRangeUi(
    val from: String,
    val to: String,
)

val WeeklyScheduleTimeOptions: List<String> = buildList {
    for (hour in 0..23) {
        for (minute in listOf(0, 30)) {
            add("%02d:%02d".format(hour, minute))
        }
    }
}

/**
 * Opciones de "Hasta" estrictamente posteriores a [start] (HH:mm zero-padded).
 * Si [start] está vacío, devuelve todas.
 */
fun endTimeOptionsAfter(
    start: String,
    all: List<String> = WeeklyScheduleTimeOptions,
): List<String> {
    if (start.isBlank()) return all
    return all.filter { it > start }
}

@Composable
fun WeeklyScheduleEditor(
    days: List<WeeklyScheduleDayUi>,
    includeSunday: Boolean,
    onIncludeSundayChange: (Boolean) -> Unit,
    onToggleDay: (String, Boolean) -> Unit,
    onRangeFromChanged: (String, Int, String) -> Unit,
    onRangeToChanged: (String, Int, String) -> Unit,
    onAddRange: (String) -> Unit,
    onRemoveRange: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    onInsertRange: (String, Int, WeeklyScheduleRangeUi) -> Unit = { _, _, _ -> },
    onCopyMondayToWeekdays: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mondayEnabled = days.any { it.key == "LUNES" && it.enabled && it.ranges.isNotEmpty() }
    val removedMessage = stringResource(R.string.weekly_schedule_range_removed)
    val undoLabel = stringResource(R.string.weekly_schedule_undo)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        OficiosCheckbox(
            checked = includeSunday,
            onCheckedChange = onIncludeSundayChange,
            label = stringResource(R.string.weekly_schedule_include_sunday),
            compactRow = true,
        )
        OficiosButton(
            text = stringResource(R.string.weekly_schedule_copy_monday),
            onClick = onCopyMondayToWeekdays,
            modifier = Modifier.fillMaxWidth(),
            enabled = mondayEnabled,
            variant = OficiosButtonVariant.Tonal,
            size = OficiosButtonSize.Form,
        )
        days.forEach { day ->
            OficiosCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    OficiosCheckbox(
                        checked = day.enabled,
                        onCheckedChange = { onToggleDay(day.key, it) },
                        label = day.label,
                        compactRow = true,
                    )
                    if (day.enabled) {
                        day.ranges.forEachIndexed { index, range ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OficiosDropdownField(
                                    value = range.from,
                                    options = WeeklyScheduleTimeOptions,
                                    onOptionSelected = { onRangeFromChanged(day.key, index, it) },
                                    label = stringResource(R.string.weekly_schedule_from),
                                    modifier = Modifier.weight(1f),
                                )
                                OficiosDropdownField(
                                    value = range.to,
                                    options = endTimeOptionsAfter(range.from),
                                    onOptionSelected = { onRangeToChanged(day.key, index, it) },
                                    label = stringResource(R.string.weekly_schedule_to),
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = {
                                        onRemoveRange(day.key, index)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = removedMessage,
                                                actionLabel = undoLabel,
                                                duration = SnackbarDuration.Short,
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onInsertRange(day.key, index, range)
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = stringResource(R.string.weekly_schedule_remove_range),
                                    )
                                }
                            }
                        }
                        OficiosButton(
                            text = stringResource(R.string.weekly_schedule_add_range),
                            onClick = { onAddRange(day.key) },
                            modifier = Modifier.fillMaxWidth(),
                            variant = OficiosButtonVariant.Tonal,
                            size = OficiosButtonSize.Form,
                        )
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.weekly_schedule_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SnackbarHost(hostState = snackbarHostState)
    }
}
