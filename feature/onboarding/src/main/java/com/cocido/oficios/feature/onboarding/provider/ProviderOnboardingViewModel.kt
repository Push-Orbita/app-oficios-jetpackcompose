package com.cocido.oficios.feature.onboarding.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.RetryPendingTradeDraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val retryPendingTradeDraftUseCase: RetryPendingTradeDraftUseCase,
    private val finalizeProviderProfileUseCase: FinalizeProviderProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderOnboardingUiState>(ProviderOnboardingUiState.Loading)
    val uiState: StateFlow<ProviderOnboardingUiState> = _uiState.asStateFlow()

    private val _isFinalizing = MutableStateFlow(false)
    val isFinalizing: StateFlow<Boolean> = _isFinalizing.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { ProviderOnboardingUiState.Loading }
            when (val result = getOnboardingStatusUseCase.getProviderStatus()) {
                is AppResult.Success -> {
                    val missing = result.data.missingSteps
                    if (missing.any { it == "rubro_principal" || it == "subrubros" }) {
                        retryPendingTradeDraftUseCase()
                    }
                    val steps = getOnboardingStatusUseCase.getProviderSteps(result.data)
                    _uiState.update { ProviderOnboardingUiState.Success(steps) }
                }
                is AppResult.Error -> {
                    _uiState.update { ProviderOnboardingUiState.Error("No se pudo cargar el estado del perfil") }
                }
            }
        }
    }

    fun finalizeProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isFinalizing.update { true }
            when (finalizeProviderProfileUseCase()) {
                is AppResult.Success -> onSuccess()
                is AppResult.Error -> load()
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
