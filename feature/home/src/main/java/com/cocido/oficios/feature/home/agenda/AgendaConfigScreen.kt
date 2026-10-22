package com.cocido.oficios.feature.home.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.feature.home.R

@Composable
fun AgendaConfigScreen(
    uiState: AgendaConfigUiState,
    onEvent: (AgendaConfigEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.agenda_config_title),
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
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
            Text(
                text = stringResource(R.string.agenda_config_by_trade_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                uiState.subRubroOptions.forEach { option ->
                    OficiosCheckbox(
                        checked = option.id in uiState.selectedSubRubroIds,
                        onCheckedChange = { onEvent(AgendaConfigEvent.OficioToggled(option.id)) },
                        label = option.subRubroLabel,
                        compactRow = true,
                    )
                }
            }
            uiState.oficioError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.agenda_config_duration_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
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
                    OficiosDropdownField(
                        value = uiState.durationUnit,
                        options = AgendaDurationUnits,
                        onOptionSelected = { onEvent(AgendaConfigEvent.DurationUnitChanged(it)) },
                        label = stringResource(R.string.agenda_config_duration_unit),
                    )
                    OficiosTextField(
                        value = uiState.intervalInput,
                        onValueChange = { onEvent(AgendaConfigEvent.IntervalChanged(it)) },
                        label = stringResource(R.string.agenda_config_interval),
                        isError = uiState.intervalError != null,
                        supportingText = uiState.intervalError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OficiosDropdownField(
                        value = uiState.intervalUnit,
                        options = AgendaDurationUnits,
                        onOptionSelected = { onEvent(AgendaConfigEvent.IntervalUnitChanged(it)) },
                        label = stringResource(R.string.agenda_config_interval_unit),
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
                    color = MaterialTheme.colorScheme.onBackground,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.agenda_config_session_duration,
                        session.durationMinutes,
                        session.durationUnit,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                session.intervalMinutes?.let { interval ->
                    Text(
                        text = stringResource(
                            R.string.agenda_config_session_interval,
                            interval,
                            session.intervalUnit ?: AgendaDurationUnitMinutes,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = OficiosColorTextSecondaryAccessible,
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.agenda_config_delete_action),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
