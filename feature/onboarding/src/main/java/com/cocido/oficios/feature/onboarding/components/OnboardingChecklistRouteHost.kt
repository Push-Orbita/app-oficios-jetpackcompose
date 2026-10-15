package com.cocido.oficios.feature.onboarding.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistEffect
import com.cocido.oficios.feature.onboarding.common.OnboardingCompletionPhase
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun OnboardingChecklistRouteHost(
    completionPhase: OnboardingCompletionPhase,
    effects: SharedFlow<OnboardingChecklistEffect>,
    onNavigate: (InitialDestination) -> Unit,
    modifier: Modifier = Modifier,
    submittingMessage: String? = null,
    checklist: @Composable () -> Unit,
) {
    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is OnboardingChecklistEffect.NavigateAfterCompletion) {
                onNavigate(effect.destination)
            }
        }
    }

    OficiosStateContent(targetState = completionPhase, modifier = modifier) { phase ->
        when (phase) {
            OnboardingCompletionPhase.Submitting -> {
                OficiosLoadingState(
                    message = submittingMessage ?: stringResource(R.string.onboarding_finalize_submitting),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is OnboardingCompletionPhase.Confirmed -> {
                OnboardingConfirmedOverlay(
                    destination = phase.destination,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            OnboardingCompletionPhase.Idle -> checklist()
        }
    }
}
