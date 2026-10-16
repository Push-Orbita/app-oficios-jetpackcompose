package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.usecase.AgendaUseCases
import com.cocido.oficios.domain.usecase.CreateServiceRequestUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkerProfileUseCase: GetWorkerProfileUseCase,
    private val clientProfileRepository: ClientProfileRepository,
    private val createServiceRequestUseCase: CreateServiceRequestUseCase,
    private val agendaUseCases: AgendaUseCases,
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L

    private val _uiState = MutableStateFlow<CreateOrderUiState>(CreateOrderUiState.Loading)
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CreateOrderEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
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
                        slotsErrorMessage = null,
                        submitIdempotencyKey = null,
                    )
                }
                loadAvailableSlots(event.id)
            }
            is CreateOrderEvent.DomicilioSelected -> updateSuccess {
                copy(selectedDomicilioId = event.id, submitIdempotencyKey = null)
            }
            is CreateOrderEvent.ContactoSelected -> updateSuccess {
                copy(selectedContactoId = event.id, submitIdempotencyKey = null)
            }
            is CreateOrderEvent.SlotSelected -> updateSuccess {
                copy(selectedSlotId = event.id, proposedDateTime = null, submitIdempotencyKey = null)
            }
            is CreateOrderEvent.DateTimeSelected -> updateSuccess {
                copy(proposedDateTime = event.dateTime, selectedSlotId = null, submitIdempotencyKey = null)
            }
            is CreateOrderEvent.DescriptionChanged -> updateSuccess {
                copy(description = event.value, submitIdempotencyKey = null)
            }
            CreateOrderEvent.Submit -> submit()
        }
    }

    private fun loadAvailableSlots(subRubroId: Long) {
        viewModelScope.launch {
            updateSuccess { copy(isLoadingSlots = true, slotsErrorMessage = null) }
            val zone = ZoneId.systemDefault()
            val from = LocalDate.now().atStartOfDay(zone).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val to = LocalDate.now().plusWeeks(4).atStartOfDay(zone).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            when (val result = agendaUseCases.getAvailableSlots(subRubroId, from, to)) {
                is AppResult.Success -> updateSuccess {
                    copy(isLoadingSlots = false, availableSlots = result.data, selectedSlotId = null, slotsErrorMessage = null)
                }
                is AppResult.Error -> updateSuccess {
                    copy(
                        isLoadingSlots = false,
                        availableSlots = emptyList(),
                        slotsErrorMessage = result.error.userMessage(),
                    )
                }
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

            _uiState.update {
                CreateOrderUiState.Success(
                    workerName = profile.displayName,
                    subRubroOptions = subRubroOptions,
                    domicilios = domicilios,
                    contactos = contactos,
                    selectedSubRubroId = selectedSubRubroId,
                    selectedDomicilioId = domicilios.singleOrNull()?.id,
                    selectedContactoId = contactos.singleOrNull()?.id,
                )
            }
            selectedSubRubroId?.let { loadAvailableSlots(it) }
        }
    }

    private fun submit() {
        val state = uiState.value
        if (state !is CreateOrderUiState.Success) return

        _uiState.update { state.copy(hasAttemptedSubmit = true) }

        val subRubroId = state.selectedSubRubroId
        val domicilioId = state.selectedDomicilioId
        val contactoId = state.selectedContactoId
        val slotId = state.selectedSlotId
        val dateTime = state.proposedDateTime
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
                val zoned = dateTime!!.atZone(ZoneId.systemDefault())
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
                    _effects.send(CreateOrderEffect.NavigateToOrderDetail(result.data))
                }
                is AppResult.Error -> {
                    _uiState.update {
                        if (it is CreateOrderUiState.Success) it.copy(isSubmitting = false) else it
                    }
                    _effects.send(CreateOrderEffect.ShowMessage(result.error.userMessage()))
                    subRubroId.let { loadAvailableSlots(it) }
                }
            }
        }
    }

    private inline fun updateSuccess(transform: CreateOrderUiState.Success.() -> CreateOrderUiState.Success) {
        _uiState.update { current -> if (current is CreateOrderUiState.Success) current.transform() else current }
    }

    private companion object {
        const val DEFAULT_DURATION_HOURS = 2L
    }
}
