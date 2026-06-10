package com.cocido.oficios.feature.onboarding.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface OnboardingGateUiState {
    data object Loading : OnboardingGateUiState
    data class Ready(val destination: InitialDestination) : OnboardingGateUiState
}

@HiltViewModel
class OnboardingGateViewModel @Inject constructor(
    private val resolveInitialDestinationUseCase: ResolveInitialDestinationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingGateUiState>(OnboardingGateUiState.Loading)
    val uiState: StateFlow<OnboardingGateUiState> = _uiState.asStateFlow()

    init {
        resolve()
    }

    fun resolve() {
        viewModelScope.launch {
            _uiState.update { OnboardingGateUiState.Loading }
            when (val result = resolveInitialDestinationUseCase()) {
                is AppResult.Success -> _uiState.update { OnboardingGateUiState.Ready(result.data) }
                is AppResult.Error -> _uiState.update { OnboardingGateUiState.Ready(InitialDestination.Login) }
            }
        }
    }
}
