package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.feature.home.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.calendar.HourChipState
import com.cocido.oficios.core.designsystem.component.calendar.OficiosHourGrid
import com.cocido.oficios.core.designsystem.component.navigation.OficiosScreenTitle
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.WorkHoursSchedule
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box

@Composable
fun AgendaConfigScreen(
    uiState: AgendaConfigUiState,
    onEvent: (AgendaConfigEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(modifier = modifier) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                AgendaConfigUiState.Loading -> OficiosLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.agenda_config_loading),
                )
                is AgendaConfigUiState.Error -> OficiosErrorState(
                    title = stringResource(R.string.agenda_config_error),
                    description = state.message,
                    onRetry = { onEvent(AgendaConfigEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                is AgendaConfigUiState.Success -> AgendaConfigContent(
                    uiState = state,
                    onEvent = onEvent,
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun AgendaConfigContent(
    uiState: AgendaConfigUiState.Success,
    onEvent: (AgendaConfigEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.subRubroOptions.isEmpty()) {
        OficiosEmptyState(
            title = stringResource(R.string.agenda_config_empty_title),
            description = stringResource(R.string.agenda_config_empty_description),
            icon = Icons.Outlined.EventBusy,
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = OficiosSpacing.OficiosScreenPadding,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        item {
            OficiosScreenTitle(
                title = stringResource(R.string.agenda_config_title),
                onNavigateBack = onNavigateBack,
            )
        }
        item {
            Text(
                text = stringResource(R.string.agenda_config_days_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val labels = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
                WorkHoursSchedule.WEEK_DAYS.forEachIndexed { index, day ->
                    val selected = day in uiState.selectedDays
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (selected) OficiosColorWhite else OficiosColorOutlineFill)
                            .clickable { onEvent(AgendaConfigEvent.DayToggled(day)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) OficiosColorBackground else OficiosColorWhite,
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.agenda_config_division_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                listOf(30 to "30 min", 60 to "1 h", 120 to "2 h").forEach { (minutes, label) ->
                    val selected = uiState.durationInput.toIntOrNull() == minutes
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected) OficiosColorBackground else OficiosColorWhite,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (selected) OficiosColorWhite else OficiosColorOutlineFill)
                            .clickable { onEvent(AgendaConfigEvent.DivisionSelected(minutes)) }
                            .padding(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xs),
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.agenda_config_hours_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            OficiosHourGrid(
                selectedHour = null,
                hourState = { hour ->
                    if (hour.hour in uiState.selectedHours) HourChipState.Selected
                    else HourChipState.Available
                },
                onHourSelected = { onEvent(AgendaConfigEvent.HourToggled(it.hour)) },
            )
        }
        if (uiState.subRubroOptions.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.agenda_config_by_trade_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                    uiState.subRubroOptions.forEach { option ->
                        val selected = option.id in uiState.selectedSubRubroIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .border(1.dp, OficiosColorOutlineFill, MaterialTheme.shapes.small)
                                .clickable { onEvent(AgendaConfigEvent.OficioToggled(option.id)) }
                                .padding(OficiosSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = option.subRubroLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = OficiosColorWhite,
                            )
                            Text(
                                text = if (selected) "✓" else "",
                                color = OficiosColorWhite,
                            )
                        }
                    }
                }
            }
        }
        if (uiState.subRubroOptions.size > 1) {
            item {
                OficiosDropdownField(
                    value = uiState.selectedSubRubro?.subRubroLabel.orEmpty(),
                    options = uiState.subRubroOptions.map { it.subRubroLabel },
                    onOptionSelected = { label ->
                        uiState.subRubroOptions.firstOrNull { it.subRubroLabel == label }?.let {
                            onEvent(AgendaConfigEvent.SubRubroSelected(it.id))
                        }
                    },
                    label = stringResource(R.string.agenda_config_specialty),
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.agenda_config_duration_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        item {
            OficiosCard {
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    OficiosTextField(
                        value = uiState.durationInput,
                        onValueChange = { onEvent(AgendaConfigEvent.DurationChanged(it)) },
                        label = stringResource(R.string.agenda_config_duration),
                        isError = uiState.durationError != null,
                        supportingText = uiState.durationError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OficiosTextField(
                        value = uiState.intervalInput,
                        onValueChange = { onEvent(AgendaConfigEvent.IntervalChanged(it)) },
                        label = stringResource(R.string.agenda_config_interval),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OficiosButton(
                        text = stringResource(R.string.agenda_config_save),
                        onClick = { onEvent(AgendaConfigEvent.Save) },
                        enabled = !uiState.isSaving,
                        loading = uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                    )
                }
            }
        }

        if (uiState.sessions.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.agenda_config_saved),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(uiState.sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    onDeleteClick = { onEvent(AgendaConfigEvent.RequestDeleteSession(session.id)) },
                )
            }
        }
    }

    if (uiState.sessionPendingDeleteId != null) {
        OficiosDialog(
            title = stringResource(R.string.agenda_config_delete_title),
            message = stringResource(R.string.agenda_config_delete_message),
            confirmLabel = stringResource(R.string.agenda_config_delete_confirm),
            confirmEnabled = !uiState.isDeleting,
            onConfirm = { onEvent(AgendaConfigEvent.ConfirmDeleteSession) },
            onDismiss = { onEvent(AgendaConfigEvent.DismissDeleteSession) },
            isDangerous = true,
        )
    }
}

@Composable
private fun SessionRow(session: AgendaSessionConfig, onDeleteClick: () -> Unit) {
    OficiosCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "${session.durationMinutes} min por turno",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                session.intervalMinutes?.let {
                    Text(
                        text = stringResource(R.string.agenda_config_interval_value, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.agenda_config_delete_title),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
