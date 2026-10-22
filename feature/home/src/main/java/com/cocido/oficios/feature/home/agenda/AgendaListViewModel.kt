package com.cocido.oficios.feature.home.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AgendaSlotStatus
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.GetMySubRubrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
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

    private var slotsJob: Job? = null

    private var skipNextResume = true

    init {
        viewModelScope.launch {
            agendaUseCases.slots.collect { snap ->
                if (snap == null) return@collect
                updateSuccess { state ->
                    if (state.selectedSubRubroId != snap.subRubroId) {
                        state
                    } else {
                        state.copy(
                            slots = snap.slots.sortedBy { slot -> slot.start },
                            isLoadingSlots = false,
                            isRefreshing = false,
                            slotsError = null,
                        )
                    }
                }
            }
        }
        load()
    }

    fun onResume() {
        if (skipNextResume) {
            skipNextResume = false
            return
        }
        refreshSlots()
    }

    fun onEvent(event: AgendaListEvent) {
        when (event) {
            AgendaListEvent.Retry -> load()
            AgendaListEvent.Refresh -> refreshAll()
            AgendaListEvent.RetrySlots -> refreshSlots()
            is AgendaListEvent.SubRubroSelected -> selectSubRubro(event.subRubroId)
            is AgendaListEvent.ViewModeChanged -> updateSuccess { it.copy(viewMode = event.mode) }
            AgendaListEvent.PreviousPeriod -> movePeriod(forward = false)
            AgendaListEvent.NextPeriod -> movePeriod(forward = true)
            AgendaListEvent.GoToToday -> goToToday()
            is AgendaListEvent.DateSelected -> selectDate(event.date)
            AgendaListEvent.OpenCreateSlot -> openCreateSlot()
            is AgendaListEvent.OpenEditSlot -> openEditSlot(event.slotId)
            AgendaListEvent.DismissSlotForm -> dismissSlotForm()
            is AgendaListEvent.SlotStartSelected -> selectSlotStart(event.dateTime)
            is AgendaListEvent.SlotEndSelected -> updateSuccess {
                it.copy(slotEnd = event.dateTime, formIssue = null, formBackendError = null)
            }
            is AgendaListEvent.SlotAutoAcceptanceChanged -> updateSuccess {
                it.copy(slotAutoAcceptance = event.value, formBackendError = null)
            }
            AgendaListEvent.SubmitSlot -> submitSlot()
            is AgendaListEvent.RequestDeleteSlot -> requestDeleteSlot(event.slotId)
            AgendaListEvent.DismissDeleteSlot -> updateSuccess { it.copy(slotPendingDeleteId = null) }
            AgendaListEvent.ConfirmDeleteSlot -> confirmDeleteSlot()
            AgendaListEvent.OpenConfig -> emitEffect(AgendaListEffect.NavigateToConfig)
        }
    }

    private fun selectSubRubro(subRubroId: Long) {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        updateSuccess {
            it.copy(
                selectedSubRubroId = subRubroId,
                slots = emptyList(),
                showSlotForm = false,
                editingSlotId = null,
            )
        }
        loadSlots(subRubroId, state.visibleMonth)
    }

    private fun selectDate(date: LocalDate) {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val newMonth = YearMonth.from(date)
        updateSuccess { it.copy(selectedDate = date, visibleMonth = newMonth) }
        if (newMonth != state.visibleMonth) state.selectedSubRubroId?.let { loadSlots(it, newMonth) }
    }

    private fun goToToday() {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val today = LocalDate.now()
        val month = YearMonth.from(today)
        updateSuccess { it.copy(selectedDate = today, visibleMonth = month) }
        if (month != state.visibleMonth) state.selectedSubRubroId?.let { loadSlots(it, month) }
    }

    private fun movePeriod(forward: Boolean) {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        when (state.viewMode) {
            AgendaViewMode.MONTH -> {
                val month = if (forward) state.visibleMonth.plusMonths(1) else state.visibleMonth.minusMonths(1)
                val selectedDay = state.selectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth())
                updateSuccess { it.copy(visibleMonth = month, selectedDate = month.atDay(selectedDay)) }
                state.selectedSubRubroId?.let { loadSlots(it, month) }
            }
            AgendaViewMode.WEEK -> {
                val date = if (forward) state.selectedDate.plusWeeks(1) else state.selectedDate.minusWeeks(1)
                selectDate(date)
            }
        }
    }

    private fun refreshAll() {
        val previous = _uiState.value
        if (previous !is AgendaListUiState.Success) {
            load(refreshing = previous is AgendaListUiState.Error)
            return
        }
        viewModelScope.launch {
            var slotsStarted = false
            try {
                updateSuccess { it.copy(isRefreshing = true) }
                when (val result = getMySubRubrosUseCase()) {
                    is AppResult.Success -> {
                        val options = result.data.filter { it.requiresTurns }
                        val selected = previous.selectedSubRubroId?.takeIf { id -> options.any { it.id == id } }
                            ?: options.firstOrNull()?.id
                        updateSuccess {
                            it.copy(
                                subRubroOptions = options,
                                selectedSubRubroId = selected,
                                isRefreshing = selected != null,
                            )
                        }
                        if (selected != null) {
                            slotsStarted = true
                            loadSlots(selected, previous.visibleMonth)
                        }
                    }
                    is AppResult.Error -> emitEffect(AgendaListEffect.ShowError(result.error.userMessage()))
                }
            } finally {
                if (!slotsStarted) {
                    updateSuccess { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    private fun load(refreshing: Boolean = false) {
        viewModelScope.launch {
            val previous = _uiState.value
            try {
                if (refreshing && previous is AgendaListUiState.Error) {
                    _uiState.value = previous.copy(isRefreshing = true)
                } else if (!refreshing) {
                    _uiState.value = AgendaListUiState.Loading
                }
                when (val result = getMySubRubrosUseCase()) {
                    is AppResult.Success -> {
                        val options = result.data.filter { it.requiresTurns }
                        val firstId = options.firstOrNull()?.id
                        _uiState.value = AgendaListUiState.Success(
                            subRubroOptions = options,
                            selectedSubRubroId = firstId,
                        )
                        if (firstId != null) loadSlots(firstId, YearMonth.now())
                    }
                    is AppResult.Error -> {
                        val message = result.error.userMessage()
                        if (previous is AgendaListUiState.Success) {
                            _uiState.value = previous.copy(isRefreshing = false)
                            emitEffect(AgendaListEffect.ShowError(message))
                        } else {
                            _uiState.value = AgendaListUiState.Error(message)
                            if (refreshing) emitEffect(AgendaListEffect.ShowError(message))
                        }
                    }
                }
            } finally {
                val state = _uiState.value
                if (state is AgendaListUiState.Error) {
                    _uiState.value = state.copy(isRefreshing = false)
                } else if (state is AgendaListUiState.Success && state.selectedSubRubroId == null) {
                    _uiState.value = state.copy(isRefreshing = false)
                }
            }
        }
    }

    private fun refreshSlots() {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        state.selectedSubRubroId?.let { loadSlots(it, state.visibleMonth) }
    }

    private fun selectSlotStart(dateTime: LocalDateTime) {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val targetMonth = YearMonth.from(dateTime)
        val adjustedEnd = if (state.slotEnd == null || !state.slotEnd.isAfter(dateTime)) {
            dateTime.plusHours(DEFAULT_SLOT_HOURS)
        } else {
            state.slotEnd
        }
        updateSuccess {
            it.copy(
                slotStart = dateTime,
                slotEnd = adjustedEnd,
                selectedDate = dateTime.toLocalDate(),
                visibleMonth = targetMonth,
                formIssue = null,
                formBackendError = null,
            )
        }
        if (targetMonth != state.visibleMonth) {
            state.selectedSubRubroId?.let { loadSlots(it, targetMonth) }
        }
    }

    private fun loadSlots(subRubroId: Long, month: YearMonth) {
        slotsJob?.cancel()
        slotsJob = viewModelScope.launch {
            try {
                updateSuccess { it.copy(isLoadingSlots = true, slotsError = null) }
                val zone = ZoneId.systemDefault()
                val from = month.atDay(1).atStartOfDay(zone).toInstant().toString()
                val to = month.atEndOfMonth().atTime(LocalTime.MAX).atZone(zone).toInstant().toString()
                when (val result = agendaUseCases.getSlotsForSubRubro(subRubroId, from, to)) {
                    is AppResult.Success -> updateSuccess {
                        it.copy(
                            isLoadingSlots = false,
                            isRefreshing = false,
                            slots = result.data.sortedBy { slot -> slot.start },
                        )
                    }
                    is AppResult.Error -> {
                        val message = result.error.userMessage()
                        updateSuccess {
                            it.copy(
                                isLoadingSlots = false,
                                isRefreshing = false,
                                slotsError = message,
                            )
                        }
                        emitEffect(AgendaListEffect.ShowError(message))
                    }
                }
            } finally {
                if (isActive) {
                    updateSuccess { it.copy(isLoadingSlots = false, isRefreshing = false) }
                }
            }
        }
    }

    private fun openCreateSlot() {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val now = LocalDateTime.now()
        val chosenDate = state.selectedDate.coerceAtLeast(now.toLocalDate())
        val start = if (chosenDate == now.toLocalDate()) {
            now.withMinute(0).withSecond(0).withNano(0).plusHours(1)
        } else {
            chosenDate.atTime(DEFAULT_START_HOUR, 0)
        }
        updateSuccess {
            it.copy(
                showSlotForm = true,
                editingSlotId = null,
                selectedDate = chosenDate,
                visibleMonth = YearMonth.from(chosenDate),
                slotStart = start,
                slotEnd = start.plusHours(DEFAULT_SLOT_HOURS),
                slotAutoAcceptance = false,
                formIssue = null,
                formBackendError = null,
            )
        }
    }

    private fun openEditSlot(slotId: Long) {
        val slot = (_uiState.value as? AgendaListUiState.Success)?.slots?.firstOrNull { it.id == slotId } ?: return
        if (slot.status != AgendaSlotStatus.DISPONIBLE || slot.end.isBefore(LocalDateTime.now())) return
        updateSuccess {
            it.copy(
                showSlotForm = true,
                editingSlotId = slot.id,
                selectedDate = slot.start.toLocalDate(),
                visibleMonth = YearMonth.from(slot.start),
                slotStart = slot.start,
                slotEnd = slot.end,
                slotAutoAcceptance = slot.autoAcceptance,
                formIssue = null,
                formBackendError = null,
            )
        }
    }

    private fun dismissSlotForm() {
        updateSuccess {
            it.copy(
                showSlotForm = false,
                editingSlotId = null,
                slotStart = null,
                slotEnd = null,
                slotAutoAcceptance = false,
                isSavingSlot = false,
                formIssue = null,
                formBackendError = null,
            )
        }
    }

    private fun submitSlot() {
        val state = _uiState.value as? AgendaListUiState.Success ?: return
        val subRubroId = state.selectedSubRubroId ?: return
        val start = state.slotStart
        val end = state.slotEnd
        val issue = when {
            start == null || end == null -> AgendaSlotFormIssue.MISSING_DATE
            start.isBefore(LocalDateTime.now()) -> AgendaSlotFormIssue.PAST_DATE
            else -> state.derivedFormIssue
        }
        if (issue != null) {
            updateSuccess { it.copy(formIssue = issue) }
            return
        }
        checkNotNull(start)
        checkNotNull(end)

        viewModelScope.launch {
            updateSuccess { it.copy(isSavingSlot = true, formIssue = null, formBackendError = null) }
            val zone = ZoneId.systemDefault()
            val result: AppResult<Unit> = if (state.editingSlotId == null) {
                when (
                    val createResult = agendaUseCases.createSlot(
                        CreateAgendaSlotParams(
                            subRubroId = subRubroId,
                            startIso = start.atZone(zone).toInstant().toString(),
                            endIso = end.atZone(zone).toInstant().toString(),
                            autoAcceptance = state.slotAutoAcceptance,
                        ),
                    )
                ) {
                    is AppResult.Success -> AppResult.Success(Unit)
                    is AppResult.Error -> AppResult.Error(createResult.error)
                }
            } else {
                agendaUseCases.updateSlot(
                    UpdateAgendaSlotParams(
                        slotId = state.editingSlotId,
                        subRubroId = subRubroId,
                        startIso = start.atZone(zone).toInstant().toString(),
                        endIso = end.atZone(zone).toInstant().toString(),
                        autoAcceptance = state.slotAutoAcceptance,
                    ),
                )
            }
            when (result) {
                is AppResult.Success -> {
                    val wasEditing = state.editingSlotId != null
                    dismissSlotForm()
                    emitEffect(AgendaListEffect.ShowMessage(if (wasEditing) "Turno actualizado" else "Turno creado"))
                    loadSlots(subRubroId, YearMonth.from(start))
                }
                is AppResult.Error -> updateSuccess {
                    it.copy(isSavingSlot = false, formBackendError = result.error.userMessage())
                }
            }
        }
    }

    private fun requestDeleteSlot(slotId: Long) {
        val slot = (_uiState.value as? AgendaListUiState.Success)?.slots?.firstOrNull { it.id == slotId } ?: return
        if (slot.status == AgendaSlotStatus.DISPONIBLE && slot.end.isAfter(LocalDateTime.now())) {
            updateSuccess { it.copy(slotPendingDeleteId = slotId) }
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
                    loadSlots(subRubroId, state.visibleMonth)
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

    private companion object {
        const val DEFAULT_START_HOUR = 9
        const val DEFAULT_SLOT_HOURS = 1L
    }
}
