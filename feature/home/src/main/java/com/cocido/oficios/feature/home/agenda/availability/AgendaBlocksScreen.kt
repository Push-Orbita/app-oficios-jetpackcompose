package com.cocido.oficios.feature.home.agenda.availability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormActionsBar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormColumn
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleTimeOptions
import com.cocido.oficios.core.ui.R as UiR
import com.cocido.oficios.domain.model.AgendaBlock
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.domain.usecase.AvailabilityUseCases
import com.cocido.oficios.feature.home.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AgendaBlocksUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val blocks: List<AgendaBlock> = emptyList(),
    val date: String = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"))
        .format(DateTimeFormatter.ISO_LOCAL_DATE),
    val fullDay: Boolean = true,
    val from: String = "09:00",
    val to: String = "18:00",
    val reason: String = "",
    val pendingDeleteId: Long? = null,
)

sealed interface AgendaBlocksEvent {
    data object Retry : AgendaBlocksEvent
    data object Back : AgendaBlocksEvent
    data object Save : AgendaBlocksEvent
    data class DateChanged(val value: String) : AgendaBlocksEvent
    data class FullDayChanged(val checked: Boolean) : AgendaBlocksEvent
    data class FromChanged(val value: String) : AgendaBlocksEvent
    data class ToChanged(val value: String) : AgendaBlocksEvent
    data class ReasonChanged(val value: String) : AgendaBlocksEvent
    data class RequestDelete(val id: Long) : AgendaBlocksEvent
    data object ConfirmDelete : AgendaBlocksEvent
    data object DismissDelete : AgendaBlocksEvent
}

sealed interface AgendaBlocksEffect {
    data object NavigateBack : AgendaBlocksEffect
    data class ShowMessage(val message: String) : AgendaBlocksEffect
}

@HiltViewModel
class AgendaBlocksViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val availabilityUseCases: AvailabilityUseCases,
) : ViewModel() {
    private val subRubroId: Long = savedStateHandle.get<Long>("subRubroId") ?: 0L
    private val _uiState = MutableStateFlow(AgendaBlocksUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<AgendaBlocksEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = availabilityUseCases.listBlocks(subRubroId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, blocks = result.data.sortedBy { block -> block.date })
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    fun onEvent(event: AgendaBlocksEvent) {
        when (event) {
            AgendaBlocksEvent.Retry -> load()
            AgendaBlocksEvent.Back -> viewModelScope.launch { _effects.send(AgendaBlocksEffect.NavigateBack) }
            AgendaBlocksEvent.Save -> save()
            is AgendaBlocksEvent.DateChanged -> _uiState.update { it.copy(date = event.value, errorMessage = null) }
            is AgendaBlocksEvent.FullDayChanged -> _uiState.update { it.copy(fullDay = event.checked, errorMessage = null) }
            is AgendaBlocksEvent.FromChanged -> _uiState.update { it.copy(from = event.value, errorMessage = null) }
            is AgendaBlocksEvent.ToChanged -> _uiState.update { it.copy(to = event.value, errorMessage = null) }
            is AgendaBlocksEvent.ReasonChanged -> _uiState.update { it.copy(reason = event.value) }
            is AgendaBlocksEvent.RequestDelete -> _uiState.update { it.copy(pendingDeleteId = event.id) }
            AgendaBlocksEvent.DismissDelete -> _uiState.update { it.copy(pendingDeleteId = null) }
            AgendaBlocksEvent.ConfirmDelete -> deletePending()
        }
    }

    private fun save() {
        val state = _uiState.value
        val start = state.from.takeUnless { state.fullDay }
        val end = state.to.takeUnless { state.fullDay }
        val validation = TimeRangeValidation.validateBlock(state.date.trim(), start, end)
        if (validation != null) {
            _uiState.update { it.copy(errorMessage = validation) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (
                val result = availabilityUseCases.createBlock(
                    CreateAgendaBlockParams(
                        subRubroId = subRubroId,
                        date = state.date.trim(),
                        start = start,
                        end = end,
                        reason = state.reason.trim().ifBlank { null },
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, reason = "", fullDay = true) }
                    _effects.send(AgendaBlocksEffect.ShowMessage("Bloqueo guardado."))
                    load()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun deletePending() {
        val id = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingDeleteId = null) }
            when (val result = availabilityUseCases.deleteBlock(subRubroId, id)) {
                is AppResult.Success -> {
                    _effects.send(AgendaBlocksEffect.ShowMessage("Bloqueo eliminado."))
                    load()
                }
                is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.userMessage()) }
            }
        }
    }
}

@Composable
fun AgendaBlocksRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: AgendaBlocksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::load)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AgendaBlocksEffect.NavigateBack -> onNavigateBack()
                is AgendaBlocksEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }
    AgendaBlocksScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
fun AgendaBlocksScreen(
    uiState: AgendaBlocksUiState,
    onEvent: (AgendaBlocksEvent) -> Unit,
) {
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.agenda_blocks_title),
                onBack = { onEvent(AgendaBlocksEvent.Back) },
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentKey = { if (it.isLoading && it.blocks.isEmpty()) "loading" else "content" },
        ) { state ->
            if (state.isLoading && state.blocks.isEmpty() && state.errorMessage == null) {
                OficiosLoadingState(modifier = Modifier.fillMaxSize())
            } else if (state.errorMessage != null && state.blocks.isEmpty() && !state.isLoading) {
                OficiosErrorState(
                    title = stringResource(R.string.agenda_availability_error),
                    description = state.errorMessage,
                    onRetry = { onEvent(AgendaBlocksEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                OficiosFormColumn(
                    footer = {
                        OficiosFormActionsBar(
                            primaryLabel = stringResource(R.string.agenda_blocks_create),
                            onPrimary = { onEvent(AgendaBlocksEvent.Save) },
                            backLabel = stringResource(R.string.agenda_back),
                            onBack = { onEvent(AgendaBlocksEvent.Back) },
                            primaryLoading = state.isSaving,
                        )
                    },
                ) {
                    Text(
                        text = stringResource(R.string.agenda_blocks_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    state.errorMessage?.takeIf { state.blocks.isNotEmpty() }?.let { OficiosInlineAlert(it) }
                    OficiosCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                            OficiosTextField(
                                value = state.date,
                                onValueChange = { onEvent(AgendaBlocksEvent.DateChanged(it)) },
                                label = stringResource(R.string.agenda_blocks_date),
                                placeholder = stringResource(R.string.agenda_blocks_date_placeholder),
                            )
                            OficiosCheckbox(
                                checked = state.fullDay,
                                onCheckedChange = { onEvent(AgendaBlocksEvent.FullDayChanged(it)) },
                                label = stringResource(R.string.agenda_blocks_full_day_toggle),
                                compactRow = true,
                            )
                            if (!state.fullDay) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                                ) {
                                    OficiosDropdownField(
                                        value = state.from,
                                        options = WeeklyScheduleTimeOptions,
                                        onOptionSelected = { onEvent(AgendaBlocksEvent.FromChanged(it)) },
                                        label = stringResource(UiR.string.weekly_schedule_from),
                                        modifier = Modifier.weight(1f),
                                    )
                                    OficiosDropdownField(
                                        value = state.to,
                                        options = WeeklyScheduleTimeOptions,
                                        onOptionSelected = { onEvent(AgendaBlocksEvent.ToChanged(it)) },
                                        label = stringResource(UiR.string.weekly_schedule_to),
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                            OficiosTextField(
                                value = state.reason,
                                onValueChange = { onEvent(AgendaBlocksEvent.ReasonChanged(it)) },
                                label = stringResource(R.string.agenda_blocks_reason),
                            )
                        }
                    }
                    if (state.blocks.isEmpty()) {
                        OficiosEmptyState(
                            title = stringResource(R.string.agenda_blocks_empty_title),
                            description = stringResource(R.string.agenda_blocks_empty_description),
                            icon = Icons.Outlined.EventBusy,
                        )
                    } else {
                        state.blocks.forEach { block ->
                            OficiosCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(block.date, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = if (block.isFullDay) {
                                                stringResource(R.string.agenda_blocks_full_day)
                                            } else {
                                                stringResource(
                                                    R.string.agenda_blocks_range,
                                                    block.start.orEmpty(),
                                                    block.end.orEmpty(),
                                                )
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        block.reason?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onEvent(AgendaBlocksEvent.RequestDelete(block.id)) }) {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = stringResource(R.string.agenda_blocks_delete),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (uiState.pendingDeleteId != null) {
        OficiosDialog(
            title = stringResource(R.string.agenda_blocks_delete_title),
            message = stringResource(R.string.agenda_blocks_delete_message),
            confirmLabel = stringResource(R.string.agenda_blocks_delete),
            onConfirm = { onEvent(AgendaBlocksEvent.ConfirmDelete) },
            onDismiss = { onEvent(AgendaBlocksEvent.DismissDelete) },
            isDangerous = true,
        )
    }
}
