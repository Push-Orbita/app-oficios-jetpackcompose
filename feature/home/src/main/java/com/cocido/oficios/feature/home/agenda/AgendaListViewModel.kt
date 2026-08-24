package com.cocido.oficios.feature.home.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.GetMySubRubrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.ZoneId
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
class AgendaListViewModel @Inject constructor(
    private val getMySubRubrosUseCase: GetMySubRubrosUseCase,
    private val agendaUseCases: AgendaUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgendaListUiState>(AgendaListUiState.Loading)
    val uiState: StateFlow<AgendaListUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AgendaListEffect>()
    val effects: SharedFlow<AgendaListEffect> = _effects.asSharedFlow()

    init {
        load()
    }

    fun onEvent(event: AgendaListEvent) {
        when (event) {
            AgendaListEvent.Retry -> load()
            is AgendaListEvent.SubRubroSelected -> selectSubRubro(event.subRubroId)
            AgendaListEvent.OpenCreateSlot -> updateSuccess { it.copy(showCreateSlotForm = true) }
            AgendaListEvent.DismissCreateSlot -> updateSuccess {
                it.copy(showCreateSlotForm = false, newSlotStart = null, newSlotEnd = null, newSlotAutoAcceptance = false)
            }
            is AgendaListEvent.NewSlotStartSelected -> updateSuccess { it.copy(newSlotStart = event.dateTime) }
            is AgendaListEvent.NewSlotEndSelected -> updateSuccess { it.copy(newSlotEnd = event.dateTime) }
            is AgendaListEvent.NewSlotAutoAcceptanceChanged -> updateSuccess { it.copy(newSlotAutoAcceptance = event.value) }
            AgendaListEvent.SubmitNewSlot -> submitNewSlot()
            is AgendaListEvent.RequestDeleteSlot -> updateSuccess { it.copy(slotPendingDeleteId = event.slotId) }
            AgendaListEvent.DismissDeleteSlot -> updateSuccess { it.copy(slotPendingDeleteId = null) }
            AgendaListEvent.ConfirmDeleteSlot -> confirmDeleteSlot()
            AgendaListEvent.OpenConfig -> emitEffect(AgendaListEffect.NavigateToConfig)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { AgendaListUiState.Loading }
            when (val result = getMySubRubrosUseCase()) {
                is AppResult.Success -> {
                    val options = result.data
                    val firstId = options.firstOrNull()?.id
                    _uiState.update {
                        AgendaListUiState.Success(subRubroOptions = options, selectedSubRubroId = firstId)
                    }
                    if (firstId != null) loadSlots(firstId)
                }
                is AppResult.Error -> _uiState.update { AgendaListUiState.Error(result.error.userMessage()) }
            }
        }
    }

    private fun selectSubRubro(subRubroId: Long) {
        updateSuccess { it.copy(selectedSubRubroId = subRubroId) }
        loadSlots(subRubroId)
    }

    private fun loadSlots(subRubroId: Long) {
        viewModelScope.launch {
            updateSuccess { it.copy(isLoadingSlots = true, error = null) }
            when (val result = agendaUseCases.getSlotsForSubRubro(subRubroId)) {
                is AppResult.Success -> updateSuccess {
                    it.copy(isLoadingSlots = false, slots = result.data.sortedBy { slot -> slot.start })
                }
                is AppResult.Error -> updateSuccess {
                    it.copy(isLoadingSlots = false, error = result.error.userMessage())
                }
            }
        }
    }

    private fun submitNewSlot() {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val subRubroId = state.selectedSubRubroId ?: return
        val start = state.newSlotStart ?: return
        val end = state.newSlotEnd ?: return
        if (!end.isAfter(start)) return

        viewModelScope.launch {
            updateSuccess { it.copy(isSavingSlot = true) }
            val zone = ZoneId.systemDefault()
            val result = agendaUseCases.createSlot(
                CreateAgendaSlotParams(
                    subRubroId = subRubroId,
                    startIso = start.atZone(zone).toInstant().toString(),
                    endIso = end.atZone(zone).toInstant().toString(),
                    autoAcceptance = state.newSlotAutoAcceptance,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    updateSuccess {
                        it.copy(
                            isSavingSlot = false,
                            showCreateSlotForm = false,
                            newSlotStart = null,
                            newSlotEnd = null,
                            newSlotAutoAcceptance = false,
                        )
                    }
                    emitEffect(AgendaListEffect.ShowMessage("Turno creado"))
                    loadSlots(subRubroId)
                }
                is AppResult.Error -> {
                    updateSuccess { it.copy(isSavingSlot = false) }
                    emitEffect(AgendaListEffect.ShowMessage(result.error.userMessage()))
                }
            }
        }
    }

    private fun confirmDeleteSlot() {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val slotId = state.slotPendingDeleteId ?: return
        val subRubroId = state.selectedSubRubroId ?: return

        viewModelScope.launch {
            updateSuccess { it.copy(slotPendingDeleteId = null) }
            when (val result = agendaUseCases.deleteSlot(slotId)) {
                is AppResult.Success -> {
                    emitEffect(AgendaListEffect.ShowMessage("Turno eliminado"))
                    loadSlots(subRubroId)
                }
                is AppResult.Error -> emitEffect(AgendaListEffect.ShowMessage(result.error.userMessage()))
            }
        }
    }

    private fun updateSuccess(block: (AgendaListUiState.Success) -> AgendaListUiState.Success) {
        _uiState.update { current -> (current as? AgendaListUiState.Success)?.let(block) ?: current }
    }

    private fun emitEffect(effect: AgendaListEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }
}
