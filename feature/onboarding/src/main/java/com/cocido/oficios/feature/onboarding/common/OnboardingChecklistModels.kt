package com.cocido.oficios.feature.onboarding.common

import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus

sealed interface OnboardingChecklistUiState {
    data object Loading : OnboardingChecklistUiState
    data class Success(val steps: List<OnboardingStep>) : OnboardingChecklistUiState
    data class Error(val message: String) : OnboardingChecklistUiState
}

sealed interface OnboardingCompletionPhase {
    data object Idle : OnboardingCompletionPhase
    data object Submitting : OnboardingCompletionPhase
    data class Confirmed(val destination: InitialDestination) : OnboardingCompletionPhase
}

sealed interface OnboardingChecklistEffect {
    data class NavigateAfterCompletion(val destination: InitialDestination) : OnboardingChecklistEffect
}

fun List<OnboardingStep>.hasActionableRequiredSteps(): Boolean =
    any { step ->
        step.isRequired &&
            (step.status == OnboardingStepStatus.PENDING || step.status == OnboardingStepStatus.REJECTED)
    }

fun List<OnboardingStep>.shouldAutoFinalizeProviderProfile(): Boolean =
    size == 1 && first().id == "finalizar_perfil"
