package com.cocido.oficios.feature.home.agenda.availability

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormActionsBar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormColumn
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleEditor
import com.cocido.oficios.domain.model.TimeRangeValidation
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.model.addRange
import com.cocido.oficios.domain.model.changeRange
import com.cocido.oficios.domain.model.removeRange
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

data class WeeklyAvailabilityUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val week: WeeklyAvailability = WeeklyAvailability.default(),
)

sealed interface WeeklyAvailabilityEvent {
    data object Retry : WeeklyAvailabilityEvent
    data object Back : WeeklyAvailabilityEvent
    data object Save : WeeklyAvailabilityEvent
    data class IncludeSunday(val included: Boolean) : WeeklyAvailabilityEvent
    data class ToggleDay(val day: String, val enabled: Boolean) : WeeklyAvailabilityEvent
    data class RangeFrom(val day: String, val index: Int, val value: String) : WeeklyAvailabilityEvent
    data class RangeTo(val day: String, val index: Int, val value: String) : WeeklyAvailabilityEvent
    data class AddRange(val day: String) : WeeklyAvailabilityEvent
    data class RemoveRange(val day: String, val index: Int) : WeeklyAvailabilityEvent
}

sealed interface WeeklyAvailabilityEffect {
    data object NavigateBack : WeeklyAvailabilityEffect
    data class Saved(val message: String) : WeeklyAvailabilityEffect
}

@HiltViewModel
class WeeklyAvailabilityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val availabilityUseCases: AvailabilityUseCases,
) : ViewModel() {
    private val subRubroId: Long = savedStateHandle.get<Long>("subRubroId") ?: 0L
    private val _uiState = MutableStateFlow(WeeklyAvailabilityUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<WeeklyAvailabilityEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { load() }

    fun load() {
        if (subRubroId <= 0L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No encontramos esa especialidad.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = availabilityUseCases.getWeekly(subRubroId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, week = result.data, errorMessage = null)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    fun onEvent(event: WeeklyAvailabilityEvent) {
        when (event) {
            WeeklyAvailabilityEvent.Retry -> load()
            WeeklyAvailabilityEvent.Back -> viewModelScope.launch {
                _effects.send(WeeklyAvailabilityEffect.NavigateBack)
            }
            WeeklyAvailabilityEvent.Save -> save()
            is WeeklyAvailabilityEvent.IncludeSunday -> _uiState.update {
                it.copy(week = it.week.copy(includeSunday = event.included), errorMessage = null)
            }
            is WeeklyAvailabilityEvent.ToggleDay -> _uiState.update {
                it.copy(week = it.week.toggleDay(event.day, event.enabled), errorMessage = null)
            }
            is WeeklyAvailabilityEvent.RangeFrom -> _uiState.update {
                it.copy(week = it.week.changeRange(event.day, event.index, from = event.value), errorMessage = null)
            }
            is WeeklyAvailabilityEvent.RangeTo -> _uiState.update {
                it.copy(week = it.week.changeRange(event.day, event.index, to = event.value), errorMessage = null)
            }
            is WeeklyAvailabilityEvent.AddRange -> _uiState.update {
                it.copy(week = it.week.addRange(event.day), errorMessage = null)
            }
            is WeeklyAvailabilityEvent.RemoveRange -> _uiState.update {
                it.copy(week = it.week.removeRange(event.day, event.index), errorMessage = null)
            }
        }
    }

    private fun save() {
        val week = _uiState.value.week
        val validation = TimeRangeValidation.validateWeek(week)
        if (validation != null) {
            _uiState.update { it.copy(errorMessage = validation) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = availabilityUseCases.replaceWeekly(subRubroId, week)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, week = result.data) }
                    _effects.send(
                        WeeklyAvailabilityEffect.Saved("Disponibilidad actualizada."),
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }
}

@Composable
fun WeeklyAvailabilityRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: WeeklyAvailabilityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::load)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WeeklyAvailabilityEffect.NavigateBack -> onNavigateBack()
                is WeeklyAvailabilityEffect.Saved -> {
                    onShowMessage(effect.message)
                    onNavigateBack()
                }
            }
        }
    }
    WeeklyAvailabilityScreen(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
fun WeeklyAvailabilityScreen(
    uiState: WeeklyAvailabilityUiState,
    onEvent: (WeeklyAvailabilityEvent) -> Unit,
) {
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.agenda_availability_title),
                onBack = { onEvent(WeeklyAvailabilityEvent.Back) },
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentKey = { if (it.isLoading) "loading" else "content" },
        ) { state ->
            if (state.isLoading) {
                OficiosLoadingState(
                    message = stringResource(R.string.agenda_availability_loading),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val labels = weekDayLabels()
                OficiosFormColumn(
                    footer = {
                        OficiosFormActionsBar(
                            primaryLabel = stringResource(R.string.agenda_availability_save),
                            onPrimary = { onEvent(WeeklyAvailabilityEvent.Save) },
                            backLabel = stringResource(R.string.agenda_back),
                            onBack = { onEvent(WeeklyAvailabilityEvent.Back) },
                            primaryLoading = state.isSaving,
                        )
                    },
                ) {
                    state.errorMessage?.let { OficiosInlineAlert(it) }
                    WeeklyScheduleEditor(
                        days = state.week.toEditorDays(labels),
                        includeSunday = state.week.includeSunday,
                        onIncludeSundayChange = { onEvent(WeeklyAvailabilityEvent.IncludeSunday(it)) },
                        onToggleDay = { day, enabled -> onEvent(WeeklyAvailabilityEvent.ToggleDay(day, enabled)) },
                        onRangeFromChanged = { day, index, value ->
                            onEvent(WeeklyAvailabilityEvent.RangeFrom(day, index, value))
                        },
                        onRangeToChanged = { day, index, value ->
                            onEvent(WeeklyAvailabilityEvent.RangeTo(day, index, value))
                        },
                        onAddRange = { onEvent(WeeklyAvailabilityEvent.AddRange(it)) },
                        onRemoveRange = { day, index -> onEvent(WeeklyAvailabilityEvent.RemoveRange(day, index)) },
                    )
                }
            }
        }
    }
}
