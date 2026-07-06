package com.cocido.oficios.feature.onboarding.common

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object OnboardingCompletionNavigator {
    const val SUCCESS_ANIMATION_DURATION_MS = 1_800L

    fun shouldShowSuccessAnimation(destination: InitialDestination): Boolean =
        destination in SUCCESS_ANIMATION_DESTINATIONS

    suspend fun navigateAfterCompletion(
        destination: InitialDestination,
        phase: MutableStateFlow<OnboardingCompletionPhase>,
        effects: MutableSharedFlow<OnboardingChecklistEffect>,
    ) {
        if (shouldShowSuccessAnimation(destination)) {
            phase.update { OnboardingCompletionPhase.Confirmed(destination) }
            delay(SUCCESS_ANIMATION_DURATION_MS)
            effects.emit(OnboardingChecklistEffect.NavigateAfterCompletion(destination))
            phase.update { OnboardingCompletionPhase.Idle }
        } else {
            effects.emit(OnboardingChecklistEffect.NavigateAfterCompletion(destination))
        }
    }

    suspend fun resolveAndNavigate(
        resolveInitialDestinationUseCase: ResolveInitialDestinationUseCase,
        stuckDestination: InitialDestination,
        fallbackDestination: InitialDestination?,
        phase: MutableStateFlow<OnboardingCompletionPhase>,
        effects: MutableSharedFlow<OnboardingChecklistEffect>,
        onStuck: suspend () -> Unit,
        onResolveError: suspend (String) -> Unit,
    ) {
        when (val destination = resolveInitialDestinationUseCase()) {
            is AppResult.Success -> {
                val target = when {
                    destination.data == stuckDestination && fallbackDestination != null -> fallbackDestination
                    else -> destination.data
                }
                if (target == stuckDestination) {
                    onStuck()
                } else {
                    navigateAfterCompletion(target, phase, effects)
                }
            }
            is AppResult.Error -> onResolveError(destination.error.userMessage())
        }
    }

    private val SUCCESS_ANIMATION_DESTINATIONS = setOf(
        InitialDestination.ClientHome,
        InitialDestination.ProviderHome,
        InitialDestination.ProviderPendingApproval,
    )
}
