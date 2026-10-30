package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.core.common.time.DisplayDateTime
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.AvailabilityUseCases
import com.cocido.oficios.domain.usecase.CreateServiceRequestUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase,
    private val clientProfileRepository: ClientProfileRepository,
    private val createServiceRequestUseCase: CreateServiceRequestUseCase,
    private val agendaUseCases: AgendaUseCases,
    private val availabilityUseCases: AvailabilityUseCases,
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L

    private val _uiState = MutableStateFlow<CreateOrderUiState>(CreateOrderUiState.Loading)
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CreateOrderEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    private var skipNextResume = true

    init {
        viewModelScope.launch {
            clientProfileRepository.domicilios.collect { list ->
                if (list == null) return@collect
                updateSuccess {
                    copy(
                        domicilios = list,
                        selectedDomicilioId = selectedDomicilioId?.takeIf { id -> list.any { it.id == id } }
                            ?: list.firstOrNull { it.favorito }?.id
                            ?: list.singleOrNull()?.id,
                    )
                }
            }
        }
        viewModelScope.launch {
            clientProfileRepository.contactos.collect { list ->
                if (list == null) return@collect
                updateSuccess {
                    copy(
                        contactos = list,
                        selectedContactoId = selectedContactoId?.takeIf { id -> list.any { it.id == id } }
                            ?: list.firstOrNull { it.favorito }?.id
                            ?: list.singleOrNull()?.id,
                    )
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
        refreshDomiciliosAndContactos()
    }

    fun onEvent(event: CreateOrderEvent) {
        when (event) {
            CreateOrderEvent.Retry -> load()
            is CreateOrderEvent.SubRubroSelected -> {
                updateSuccess {
                    copy(
                        selectedSubRubroId = event.id,
                        selectedSlotId = null,
                        proposedDateTime = null,
                        availableSlots = emptyList(),
                        monthSlots = emptyList(),
                        dayRanges = emptyList(),
                        dayHasAvailability = null,
                        slotsErrorMessage = null,
                        submitErrorMessage = null,
                        submitIdempotencyKey = null,
                        specialtyLabel = subRubroOptions.firstOrNull { it.id == event.id }?.name.orEmpty(),
                    )
                }
                loadAvailableSlots(event.id)
                val date = (uiState.value as? CreateOrderUiState.Success)?.selectedDate
                if (date != null) loadDaySchedule(event.id, date)
            }
            is CreateOrderEvent.DomicilioSelected -> updateSuccess {
                copy(selectedDomicilioId = event.id, submitIdempotencyKey = null)
            }
            is CreateOrderEvent.ContactoSelected -> updateSuccess {
                copy(selectedContactoId = event.id, submitIdempotencyKey = null)
            }
            is CreateOrderEvent.DateSelected -> {
                updateSuccess {
                    applyDateHour(selectedDate = event.date, selectedHour = selectedHour)
                }
                val subRubroId = (uiState.value as? CreateOrderUiState.Success)?.selectedSubRubroId
                if (subRubroId != null) loadDaySchedule(subRubroId, event.date)
            }
            is CreateOrderEvent.HourSelected -> updateSuccess {
                applyDateHour(selectedDate = selectedDate, selectedHour = event.hour)
            }
            is CreateOrderEvent.MonthChanged -> updateSuccess {
                copy(visibleMonth = event.month)
            }
            is CreateOrderEvent.DescriptionChanged -> updateSuccess {
                copy(description = event.value, submitIdempotencyKey = null)
            }
            CreateOrderEvent.ContinueToSummary -> continueToSummary()
            CreateOrderEvent.EditRequest -> updateSuccess {
                copy(step = CreateOrderStep.FORM)
            }
            CreateOrderEvent.Submit -> submit()
            CreateOrderEvent.AddLocation -> viewModelScope.launch {
                _effects.send(CreateOrderEffect.NavigateToAddLocation)
            }
            is CreateOrderEvent.LocationPicked -> refreshDomicilios(event.id)
        }
    }

    private fun refreshDomicilios(preferredId: Long) {
        viewModelScope.launch {
            val result = clientProfileRepository.getMyDomicilios()
            val list = (result as? AppResult.Success)?.data.orEmpty()
            updateSuccess {
                copy(
                    domicilios = list,
                    selectedDomicilioId = list.firstOrNull { it.id == preferredId }?.id
                        ?: selectedDomicilioId?.takeIf { id -> list.any { it.id == id } }
                        ?: list.firstOrNull { it.favorito }?.id
                        ?: list.singleOrNull()?.id,
                    submitIdempotencyKey = null,
                )
            }
        }
    }

    private fun refreshDomiciliosAndContactos() {
        viewModelScope.launch {
            val domiciliosResult = clientProfileRepository.getMyDomicilios()
            val contactosResult = clientProfileRepository.getMyContactos()
            val domicilios = (domiciliosResult as? AppResult.Success)?.data
            val contactos = (contactosResult as? AppResult.Success)?.data
            updateSuccess {
                copy(
                    domicilios = domicilios ?: this.domicilios,
                    contactos = contactos ?: this.contactos,
                    selectedDomicilioId = (domicilios ?: this.domicilios).let { list ->
                        selectedDomicilioId?.takeIf { id -> list.any { it.id == id } }
                            ?: list.firstOrNull { it.favorito }?.id
                            ?: list.singleOrNull()?.id
                    },
                    selectedContactoId = (contactos ?: this.contactos).let { list ->
                        selectedContactoId?.takeIf { id -> list.any { it.id == id } }
                            ?: list.firstOrNull { it.favorito }?.id
                            ?: list.singleOrNull()?.id
                    },
                )
            }
        }
    }

    private fun CreateOrderUiState.Success.applyDateHour(
        selectedDate: LocalDate?,
        selectedHour: java.time.LocalTime?,
    ): CreateOrderUiState.Success {
        val hour = selectedHour?.takeIf {
            CreateOrderSchedule.isHourSelectable(
                hour = it,
                date = selectedDate,
                slots = availableSlots,
                ranges = dayRanges,
                dayHasAvailability = dayHasAvailability,
            )
        }
        val slot = CreateOrderSchedule.matchingSlot(availableSlots, selectedDate, hour)
        return copy(
            selectedDate = selectedDate,
            selectedHour = hour,
            selectedSlotId = slot?.id,
            proposedDateTime = if (slot == null) {
                CreateOrderSchedule.visitDateTime(selectedDate, hour)
            } else {
                null
            },
            submitIdempotencyKey = null,
        )
    }

    private fun loadAvailableSlots(subRubroId: Long) {
        viewModelScope.launch {
            updateSuccess { copy(isLoadingSlots = true, slotsErrorMessage = null) }
            val zone = DisplayDateTime.ARGENTINA
            val from = LocalDate.now(DisplayDateTime.ARGENTINA).atStartOfDay(zone).toOffsetDateTime()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val to = LocalDate.now(DisplayDateTime.ARGENTINA).plusWeeks(4).atStartOfDay(zone).toOffsetDateTime()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            when (val result = agendaUseCases.getAvailableSlots(subRubroId, from, to)) {
                is AppResult.Success -> updateSuccess {
                    val next = copy(
                        isLoadingSlots = false,
                        monthSlots = result.data,
                        availableSlots = if (selectedDate == null) result.data else availableSlots.ifEmpty { result.data },
                        hasAgenda = result.data.any { it.isSelectable } || hasAgenda,
                        slotsErrorMessage = null,
                    )
                    next.applyDateHour(next.selectedDate, next.selectedHour)
                }
                is AppResult.Error -> updateSuccess {
                    copy(
                        isLoadingSlots = false,
                        monthSlots = emptyList(),
                        slotsErrorMessage = result.error.userMessage(),
                    )
                }
            }
        }
    }

    private fun loadDaySchedule(subRubroId: Long, date: LocalDate) {
        viewModelScope.launch {
            updateSuccess { copy(isLoadingSlots = true, slotsErrorMessage = null) }
            val dateIso = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayResult = availabilityUseCases.getDay(workerId, dateIso, subRubroId)
            val slotsResult = agendaUseCases.getAvailableSlotsForDate(subRubroId, dateIso)
            updateSuccess {
                if (selectedDate != date) return@updateSuccess this
                val window = (dayResult as? AppResult.Success)?.data
                val ranges = window?.ranges.orEmpty()
                val slots = (slotsResult as? AppResult.Success)?.data
                    ?: availableSlots.filter { it.start.toLocalDate() == date }
                val dayKnown = dayResult is AppResult.Success || slotsResult is AppResult.Success
                val hasAvailability = when {
                    slots.any { it.isSelectable } -> true
                    ranges.isNotEmpty() -> true
                    dayResult is AppResult.Success -> false
                    else -> null
                }
                copy(
                    isLoadingSlots = false,
                    availableSlots = slots,
                    dayRanges = ranges,
                    dayHasAvailability = hasAvailability,
                    hasAgenda = window?.requiresTurns == true || slots.any { it.isSelectable },
                    slotsErrorMessage = if (dayKnown && hasAvailability == false) {
                        "Ese día no tiene disponibilidad."
                    } else {
                        null
                    },
                ).applyDateHour(selectedDate, selectedHour)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { CreateOrderUiState.Loading }
            val profileResult = getWorkerProfileUseCase(workerId)
            if (profileResult is AppResult.Error) {
                _uiState.update { CreateOrderUiState.Error(profileResult.error.userMessage()) }
                return@launch
            }
            val profile = (profileResult as AppResult.Success).data
            val subRubroOptions = profile.specialties.flatMap { it.subRubros }

            val domiciliosResult = clientProfileRepository.getMyDomicilios()
            val contactosResult = clientProfileRepository.getMyContactos()
            val domicilios = (domiciliosResult as? AppResult.Success)?.data.orEmpty()
            val contactos = (contactosResult as? AppResult.Success)?.data.orEmpty()
            val selectedSubRubroId = subRubroOptions.singleOrNull()?.id
            val workDayKeys = profile.availability?.workDays
                ?.flatMap { expandWorkDayRange(it.dayStart, it.dayEnd) }
                ?.toSet()
                .orEmpty()

            _uiState.update {
                CreateOrderUiState.Success(
                    workerName = profile.displayName,
                    workerAvatarUrl = profile.avatarUrl,
                    workerVerified = profile.isVerified,
                    workerRating = profile.reputation?.rating,
                    workerReviewCount = profile.reputation?.reviewCount,
                    specialtyLabel = subRubroOptions.singleOrNull()?.name.orEmpty(),
                    subRubroOptions = subRubroOptions,
                    domicilios = domicilios,
                    contactos = contactos,
                    workDayKeys = workDayKeys,
                    selectedSubRubroId = selectedSubRubroId,
                    selectedDomicilioId = domicilios.firstOrNull { it.favorito }?.id
                        ?: domicilios.singleOrNull()?.id,
                    selectedContactoId = contactos.firstOrNull { it.favorito }?.id
                        ?: contactos.singleOrNull()?.id,
                )
            }
            selectedSubRubroId?.let { loadAvailableSlots(it) }
        }
    }

    private fun continueToSummary() {
        val state = uiState.value
        if (state !is CreateOrderUiState.Success) return
        _uiState.update { state.copy(hasAttemptedContinue = true) }
        if (!CreateOrderSchedule.canContinue(
                selectedSubRubroId = state.selectedSubRubroId,
                selectedDomicilioId = state.selectedDomicilioId,
                selectedContactoId = state.selectedContactoId,
                selectedDate = state.selectedDate,
                selectedHour = state.selectedHour,
            ) || !CreateOrderSchedule.isHourSelectable(
                hour = state.selectedHour,
                date = state.selectedDate,
                slots = state.availableSlots,
                ranges = state.dayRanges,
                dayHasAvailability = state.dayHasAvailability,
            )
        ) {
            return
        }
        updateSuccess { copy(step = CreateOrderStep.SUMMARY) }
    }

    private fun submit() {
        val state = uiState.value
        if (state !is CreateOrderUiState.Success) return
        if (state.step != CreateOrderStep.SUMMARY) {
            continueToSummary()
            return
        }

        _uiState.update { state.copy(hasAttemptedSubmit = true) }

        val subRubroId = state.selectedSubRubroId
        val domicilioId = state.selectedDomicilioId
        val contactoId = state.selectedContactoId
        val slotId = state.selectedSlotId?.takeIf { it > 0L }
        val dateTime = state.proposedDateTime
            ?: CreateOrderSchedule.visitDateTime(state.selectedDate, state.selectedHour)
        val hasSlotOrDate = slotId != null || dateTime != null
        if (subRubroId == null || domicilioId == null || contactoId == null || !hasSlotOrDate) {
            return
        }

        viewModelScope.launch {
            val key = state.submitIdempotencyKey ?: UUID.randomUUID().toString()
            _uiState.update {
                if (it is CreateOrderUiState.Success) it.copy(isSubmitting = true, submitIdempotencyKey = key) else it
            }

            val result = if (slotId != null) {
                createServiceRequestUseCase(
                    CreateServiceRequestParams(
                        trabajadorOficioSubRubroId = subRubroId,
                        domicilioId = domicilioId,
                        contactoId = contactoId,
                        slotTurnoId = slotId,
                        description = state.description.ifBlank { null },
                        idempotencyKey = key,
                    ),
                )
            } else {
                val zoned = dateTime!!.atZone(DisplayDateTime.ARGENTINA)
                createServiceRequestUseCase(
                    CreateServiceRequestParams(
                        trabajadorOficioSubRubroId = subRubroId,
                        domicilioId = domicilioId,
                        contactoId = contactoId,
                        proposedStartIso = zoned.toInstant().toString(),
                        proposedEndIso = zoned.plusHours(DEFAULT_DURATION_HOURS).toInstant().toString(),
                        description = state.description.ifBlank { null },
                        idempotencyKey = key,
                    ),
                )
            }
            when (result) {
                is AppResult.Success -> {
                    _effects.send(CreateOrderEffect.RequestSent)
                }
                is AppResult.Error -> {
                    val message = result.error.userMessage()
                    _uiState.update {
                        if (it is CreateOrderUiState.Success) {
                            it.copy(isSubmitting = false, submitErrorMessage = message)
                        } else {
                            it
                        }
                    }
                    _effects.send(CreateOrderEffect.ShowMessage(message))
                    val date = (uiState.value as? CreateOrderUiState.Success)?.selectedDate
                    if (date != null) loadDaySchedule(subRubroId, date) else loadAvailableSlots(subRubroId)
                }
            }
        }
    }

    private inline fun updateSuccess(transform: CreateOrderUiState.Success.() -> CreateOrderUiState.Success) {
        _uiState.update { current -> if (current is CreateOrderUiState.Success) current.transform() else current }
    }

    private fun expandWorkDayRange(dayStart: String, dayEnd: String): List<String> {
        val weekDays = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")
        val start = weekDays.indexOf(dayStart.uppercase())
        val end = weekDays.indexOf(dayEnd.uppercase())
        
        if (start < 0 || end < 0) {
            return if (start >= 0) listOf(weekDays[start]) else emptyList()
        }
        
        return if (start <= end) {
            weekDays.subList(start, end + 1)
        } else {
            listOf(weekDays[start])
        }
    }

    private companion object {
        const val DEFAULT_DURATION_HOURS = 2L
    }
}
