package com.cocido.oficios.feature.home.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.GetMySubRubrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AgendaConfigViewModel @Inject constructor(
    private val getMySubRubrosUseCase: GetMySubRubrosUseCase,
    private val agendaUseCases: AgendaUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgendaConfigUiState>(AgendaConfigUiState.Loading)
    val uiState: StateFlow<AgendaConfigUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AgendaConfigEffect>()
    val effects: SharedFlow<AgendaConfigEffect> = _effects.asSharedFlow()

    init {
        load()
    }

    fun onEvent(event: AgendaConfigEvent) {
        when (event) {
            AgendaConfigEvent.Retry -> load()
            is AgendaConfigEvent.SubRubroSelected -> selectSubRubro(event.subRubroId)
            is AgendaConfigEvent.DurationChanged -> updateSuccess {
                it.copy(durationInput = event.value.filter(Char::isDigit), hasAttemptedSubmit = false)
            }
            is AgendaConfigEvent.IntervalChanged -> updateSuccess {
                it.copy(intervalInput = event.value.filter(Char::isDigit))
            }
            AgendaConfigEvent.Save -> save()
            is AgendaConfigEvent.DeleteSession -> deleteSession(event.sessionId)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { AgendaConfigUiState.Loading }
            when (val result = getMySubRubrosUseCase()) {
                is AppResult.Success -> {
                    val options = result.data
                    val firstId = options.firstOrNull()?.id
                    _uiState.update {
                        AgendaConfigUiState.Success(subRubroOptions = options, selectedSubRubroId = firstId)
                    }
                    if (firstId != null) loadSessions(firstId)
                }
                is AppResult.Error -> _uiState.update { AgendaConfigUiState.Error(result.error.userMessage()) }
            }
        }
    }

    private fun selectSubRubro(subRubroId: Long) {
        updateSuccess { it.copy(selectedSubRubroId = subRubroId, durationInput = "60", intervalInput = "") }
        loadSessions(subRubroId)
    }

    private fun loadSessions(subRubroId: Long) {
        viewModelScope.launch {
            updateSuccess { it.copy(isLoadingSessions = true, error = null) }
            when (val result = agendaUseCases.getSessionsForSubRubro(subRubroId)) {
                is AppResult.Success -> updateSuccess { it.copy(isLoadingSessions = false, sessions = result.data) }
                is AppResult.Error -> updateSuccess {
                    it.copy(isLoadingSessions = false, error = result.error.userMessage())
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value as? AgendaConfigUiState.Success ?: return
        if (!state.canSubmit) {
            updateSuccess { it.copy(hasAttemptedSubmit = true) }
            return
        }
        val subRubroId = state.selectedSubRubroId ?: return
        val duration = state.durationInput.toInt()
        val interval = state.intervalInput.toIntOrNull()

        viewModelScope.launch {
            updateSuccess { it.copy(isSaving = true) }
            val result = agendaUseCases.saveSession(
                SaveAgendaSessionParams(subRubroId = subRubroId, durationMinutes = duration, intervalMinutes = interval),
            )
            when (result) {
                is AppResult.Success -> {
                    updateSuccess { it.copy(isSaving = false, hasAttemptedSubmit = false) }
                    emitEffect(AgendaConfigEffect.ShowMessage("Configuración guardada"))
                    loadSessions(subRubroId)
                }
                is AppResult.Error -> {
                    updateSuccess { it.copy(isSaving = false) }
                    emitEffect(AgendaConfigEffect.ShowMessage(result.error.userMessage()))
                }
            }
        }
    }

    private fun deleteSession(sessionId: Long) {
        val subRubroId = (_uiState.value as? AgendaConfigUiState.Success)?.selectedSubRubroId ?: return
        viewModelScope.launch {
            when (val result = agendaUseCases.deleteSession(sessionId)) {
                is AppResult.Success -> {
                    emitEffect(AgendaConfigEffect.ShowMessage("Configuración eliminada"))
                    loadSessions(subRubroId)
                }
                is AppResult.Error -> emitEffect(AgendaConfigEffect.ShowMessage(result.error.userMessage()))
            }
        }
    }

    private fun updateSuccess(block: (AgendaConfigUiState.Success) -> AgendaConfigUiState.Success) {
        _uiState.update { current -> (current as? AgendaConfigUiState.Success)?.let(block) ?: current }
    }

    private fun emitEffect(effect: AgendaConfigEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
}
