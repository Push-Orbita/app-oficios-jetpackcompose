package com.cocido.oficios.feature.onboarding.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistEffect
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState
import com.cocido.oficios.feature.onboarding.common.OnboardingCompletionNavigator
import com.cocido.oficios.feature.onboarding.common.OnboardingCompletionPhase
import com.cocido.oficios.feature.onboarding.common.hasActionableRequiredSteps
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
class ClientOnboardingViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val resolveInitialDestinationUseCase: ResolveInitialDestinationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingChecklistUiState>(OnboardingChecklistUiState.Loading)
    val uiState: StateFlow<OnboardingChecklistUiState> = _uiState.asStateFlow()

    private val _completionPhase = MutableStateFlow<OnboardingCompletionPhase>(OnboardingCompletionPhase.Idle)
    val completionPhase: StateFlow<OnboardingCompletionPhase> = _completionPhase.asStateFlow()

    private val _effects = MutableSharedFlow<OnboardingChecklistEffect>(replay = 1)
    val effects: SharedFlow<OnboardingChecklistEffect> = _effects.asSharedFlow()

    init {
        load()
    }

    fun load() {
        if (_completionPhase.value != OnboardingCompletionPhase.Idle) return

        viewModelScope.launch {
            _uiState.update { OnboardingChecklistUiState.Loading }
            when (val result = getOnboardingStatusUseCase.getClientStatus()) {
                is AppResult.Success -> {
                    val steps = getOnboardingStatusUseCase.getClientSteps(result.data)
                    if (steps.hasActionableRequiredSteps()) {
                        _uiState.update { OnboardingChecklistUiState.Success(steps) }
                    } else {
                        completeAndNavigate()
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { OnboardingChecklistUiState.Error("No se pudo cargar el estado del perfil") }
                }
            }
        }
    }

    private suspend fun completeAndNavigate() {
        OnboardingCompletionNavigator.resolveAndNavigate(
            resolveInitialDestinationUseCase = resolveInitialDestinationUseCase,
            stuckDestination = InitialDestination.ClientOnboarding,
            fallbackDestination = InitialDestination.ClientHome,
            phase = _completionPhase,
            effects = _effects,
            onStuck = {
                _uiState.update {
                    OnboardingChecklistUiState.Error(
                        "No encontramos pasos pendientes, pero su perfil aún no está listo para operar.",
                    )
                }
            },
            onResolveError = { message ->
                _uiState.update { OnboardingChecklistUiState.Error(message) }
            },
        )
    }
}
