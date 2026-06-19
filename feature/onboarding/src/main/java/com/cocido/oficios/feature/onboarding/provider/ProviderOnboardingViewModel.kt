package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.RetryPendingTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProviderOnboardingUiState {
    data object Loading : ProviderOnboardingUiState
    data class Success(val steps: List<OnboardingStep>) : ProviderOnboardingUiState
    data class Error(val message: String) : ProviderOnboardingUiState
}

sealed interface ProviderStatusUiState {
    data object Loading : ProviderStatusUiState
    data class Pending(val steps: List<OnboardingStep>) : ProviderStatusUiState
    data class Rejected(val reason: String?, val steps: List<OnboardingStep>) : ProviderStatusUiState
    data class Error(val message: String) : ProviderStatusUiState
}

@HiltViewModel
class ProviderOnboardingViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
    private val retryPendingTradeDraftUseCase: RetryPendingTradeDraftUseCase,
    private val finalizeProviderProfileUseCase: FinalizeProviderProfileUseCase,
    private val resolveInitialDestinationUseCase: ResolveInitialDestinationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderOnboardingUiState>(ProviderOnboardingUiState.Loading)
    val uiState: StateFlow<ProviderOnboardingUiState> = _uiState.asStateFlow()

    private val _isFinalizing = MutableStateFlow(false)
    val isFinalizing: StateFlow<Boolean> = _isFinalizing.asStateFlow()

    private val _finalizeError = MutableStateFlow<String?>(null)
    val finalizeError: StateFlow<String?> = _finalizeError.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { ProviderOnboardingUiState.Loading }
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val status = syncTradeDraftIfNeeded(result.data)
                    val steps = getOnboardingStatusUseCase.getProviderSteps(status)
                    _uiState.update { ProviderOnboardingUiState.Success(steps) }
                }
                is AppResult.Error -> {
                    _uiState.update { ProviderOnboardingUiState.Error("No se pudo cargar el estado del perfil") }
                }
            }
        }
    }

    private suspend fun syncTradeDraftIfNeeded(
        status: com.cocido.oficios.domain.model.ProviderProfileStatus,
    ): com.cocido.oficios.domain.model.ProviderProfileStatus {
        syncRegisterTradeDraftFromProfileUseCase()

        if (status.missingSteps.none { it in TRADE_DRAFT_MISSING_STEPS }) return status

        val draft = registerTradeDraftRepository.get()
        if (draft == null || draft.subRubroIds.isEmpty()) return status

        retryPendingTradeDraftUseCase()

        return when (val refreshed = getOnboardingStatusUseCase.getProviderStatus()) {
            is AppResult.Success -> refreshed.data
            is AppResult.Error -> status
        }
    }

    private companion object {
        val TRADE_DRAFT_MISSING_STEPS = setOf(
            "rubro_principal",
            "subrubros",
            "disponibilidad",
            "agenda_turnos",
            "metodos_pago",
            "descripcion",
            "experiencia",
        )
    }

    fun finalizeProfile(onResolved: (InitialDestination) -> Unit) {
        viewModelScope.launch {
            _isFinalizing.update { true }
            _finalizeError.update { null }
            when (finalizeProviderProfileUseCase()) {
                is AppResult.Success -> {
                    when (val destination = resolveInitialDestinationUseCase()) {
                        is AppResult.Success -> onResolved(destination.data)
                        is AppResult.Error -> {
                            _finalizeError.update { destination.error.userMessage() }
                            load()
                        }
                    }
                }
                is AppResult.Error -> {
                    _finalizeError.update { "No se pudo finalizar el perfil. Revisá los pasos pendientes." }
                    load()
                }
            }
            _isFinalizing.update { false }
        }
    }
}

@HiltViewModel
class ProviderPendingViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderStatusUiState>(ProviderStatusUiState.Loading)
    val uiState: StateFlow<ProviderStatusUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val steps = getOnboardingStatusUseCase.getProviderSteps(result.data)
                    _uiState.update { ProviderStatusUiState.Pending(steps) }
                }
                is AppResult.Error -> {
                    _uiState.update { ProviderStatusUiState.Error("No se pudo cargar el estado") }
                }
            }
        }
    }
}

@HiltViewModel
class ProviderRejectedViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderStatusUiState>(ProviderStatusUiState.Loading)
    val uiState: StateFlow<ProviderStatusUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val steps = getOnboardingStatusUseCase.getProviderSteps(result.data)
                    _uiState.update {
                        ProviderStatusUiState.Rejected(
                            reason = result.data.rejectionReason,
                            steps = steps,
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { ProviderStatusUiState.Error("No se pudo cargar el estado") }
                }
            }
        }
    }
}
