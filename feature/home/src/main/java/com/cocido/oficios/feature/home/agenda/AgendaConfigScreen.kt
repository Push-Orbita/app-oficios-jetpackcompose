package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.feature.home.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.calendar.HourChipState
import com.cocido.oficios.core.designsystem.component.calendar.OficiosHourGrid
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import com.cocido.oficios.core.designsystem.component.navigation.OficiosScreenTitle
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.model.WorkHoursSchedule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy

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

    val chipShape = RoundedCornerShape(OficiosRadius.sm)
    val sectionShape = RoundedCornerShape(OficiosRadius.lg)

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
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OficiosColorOutlineFill, sectionShape)
                    .padding(OficiosSpacing.s10),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val labels = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sá")
                WorkHoursSchedule.WEEK_DAYS.forEachIndexed { index, day ->
                    val selected = day in uiState.selectedDays
                    Box(
                        modifier = Modifier
                            .height(OficiosComponentSize.agendaDayChipHeight)
                            .clip(chipShape)
                            .background(if (selected) OficiosColorAction else OficiosColorOutlineFill.copy(alpha = 0f))
                            .clickable { onEvent(AgendaConfigEvent.DayToggled(day)) }
                            .padding(horizontal = OficiosSpacing.xs, vertical = OficiosSpacing.xxs),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.bodyLarge,
                            color = OficiosColorWhite,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OficiosColorOutlineFill, sectionShape)
                    .padding(OficiosSpacing.s10),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    listOf(
                        30 to stringResource(R.string.agenda_config_division_30),
                        60 to stringResource(R.string.agenda_config_division_60),
                        120 to stringResource(R.string.agenda_config_division_120),
                    ).forEach { (minutes, label) ->
                        val selected = uiState.durationInput.toIntOrNull() == minutes
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OficiosColorWhite,
                            modifier = Modifier
                                .clip(chipShape)
                                .background(if (selected) OficiosColorAction else OficiosColorOutlineFill.copy(alpha = 0f))
                                .clickable { onEvent(AgendaConfigEvent.DivisionSelected(minutes)) }
                                .padding(horizontal = OficiosSpacing.sm, vertical = OficiosSpacing.xs),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(OficiosComponentSize.iconBox)
                        .clip(chipShape)
                        .background(OficiosColorOutlineFill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(OficiosIcons.Clock),
                        contentDescription = null,
                        tint = OficiosColorWhite,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
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
                    if (hour.hour in uiState.selectedHours) HourChipState.Available
                    else HourChipState.Idle
                },
                onHourSelected = { onEvent(AgendaConfigEvent.HourToggled(it.hour)) },
            )
            Text(
                text = stringResource(R.string.agenda_config_hours_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorTextSecondaryAccessible,
                modifier = Modifier.padding(top = OficiosSpacing.xs),
            )
        }
        item {
            Text(
                text = stringResource(R.string.agenda_config_applies_note),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorWhite,
            )
        }
        item {
            Text(
                text = stringResource(R.string.agenda_config_by_trade_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                uiState.subRubroOptions.forEach { option ->
                    OficioCalendarRow(
                        option = option,
                        selected = option.id in uiState.selectedSubRubroIds,
                        onToggle = { onEvent(AgendaConfigEvent.OficioToggled(option.id)) },
                    )
                }
            }
        }
        item {
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
private fun OficioCalendarRow(
    option: ProviderSubRubroOption,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val shape = RoundedCornerShape(OficiosRadius.sm)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, OficiosColorOutlineFill, shape)
            .clickable(onClick = onToggle)
            .padding(OficiosSpacing.s10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = OficiosColorWhite,
                uncheckedColor = OficiosColorWhite,
                checkmarkColor = OficiosColorAction,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.subRubroLabel,
                style = MaterialTheme.typography.titleMedium,
                color = OficiosColorWhite,
            )
            Text(
                text = stringResource(R.string.agenda_config_general_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorTextSecondaryAccessible,
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(OficiosIcons.ChevronRight),
            contentDescription = null,
            tint = OficiosColorWhite,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
        )
    }
}
