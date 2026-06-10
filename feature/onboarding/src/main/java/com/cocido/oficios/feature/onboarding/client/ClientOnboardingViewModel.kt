package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ClientOnboardingUiState {
    data object Loading : ClientOnboardingUiState
    data class Success(val steps: List<OnboardingStep>) : ClientOnboardingUiState
    data class Error(val message: String) : ClientOnboardingUiState
}

@HiltViewModel
class ClientOnboardingViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientOnboardingUiState>(ClientOnboardingUiState.Loading)
    val uiState: StateFlow<ClientOnboardingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { ClientOnboardingUiState.Loading }
            when (val result = getOnboardingStatusUseCase.getClientStatus()) {
                is AppResult.Success -> {
                    val steps = getOnboardingStatusUseCase.getClientSteps(result.data)
                    _uiState.update { ClientOnboardingUiState.Success(steps) }
                }
                is AppResult.Error -> {
                    _uiState.update { ClientOnboardingUiState.Error("No se pudo cargar el estado del perfil") }
                }
            }
        }
    }
}
