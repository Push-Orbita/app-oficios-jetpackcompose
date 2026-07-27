package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.usecase.CreateServiceRequestUseCase
import com.cocido.oficios.domain.usecase.worker.GetWorkerProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
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
            is CreateOrderEvent.SubRubroSelected -> updateSuccess { copy(selectedSubRubroId = event.id) }
            is CreateOrderEvent.DomicilioSelected -> updateSuccess { copy(selectedDomicilioId = event.id) }
            is CreateOrderEvent.ContactoSelected -> updateSuccess { copy(selectedContactoId = event.id) }
            is CreateOrderEvent.DateTimeSelected -> updateSuccess { copy(proposedDateTime = event.dateTime) }
            is CreateOrderEvent.DescriptionChanged -> updateSuccess { copy(description = event.value) }
            CreateOrderEvent.Submit -> submit()
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

            _uiState.update {
                CreateOrderUiState.Success(
                    workerName = profile.displayName,
                    subRubroOptions = subRubroOptions,
                    domicilios = domicilios,
                    contactos = contactos,
                    selectedSubRubroId = subRubroOptions.singleOrNull()?.id,
                    selectedDomicilioId = domicilios.singleOrNull()?.id,
                    selectedContactoId = contactos.singleOrNull()?.id,
                )
            }
        }
    }

    private fun submit() {
        val state = uiState.value
        if (state !is CreateOrderUiState.Success) return

        _uiState.update { state.copy(hasAttemptedSubmit = true) }

        val subRubroId = state.selectedSubRubroId
        val domicilioId = state.selectedDomicilioId
        val contactoId = state.selectedContactoId
        val dateTime = state.proposedDateTime
        if (subRubroId == null || domicilioId == null || contactoId == null || dateTime == null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state.copy(isSubmitting = true) }
            val zoned = dateTime.atZone(ZoneId.systemDefault())
            val startIso = zoned.toInstant().toString()
            val endIso = zoned.plusHours(DEFAULT_DURATION_HOURS).toInstant().toString()

            val result = createServiceRequestUseCase(
                CreateServiceRequestParams(
                    trabajadorOficioSubRubroId = subRubroId,
                    domicilioId = domicilioId,
                    contactoId = contactoId,
                    proposedStartIso = startIso,
                    proposedEndIso = endIso,
                    description = state.description.ifBlank { null },
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    _effects.send(CreateOrderEffect.NavigateToOrderDetail(result.data))
                }
                is AppResult.Error -> {
                    _uiState.update { state.copy(isSubmitting = false) }
                    _effects.send(CreateOrderEffect.ShowMessage(result.error.userMessage()))
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
