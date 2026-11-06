package com.cocido.oficios.feature.onboarding.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistEffect
import com.cocido.oficios.feature.onboarding.common.OnboardingCompletionPhase
import kotlinx.coroutines.flow.Flow

@Composable
fun OnboardingChecklistRouteHost(
    completionPhase: OnboardingCompletionPhase,
    effects: Flow<OnboardingChecklistEffect>,
    onNavigate: (InitialDestination) -> Unit,
    modifier: Modifier = Modifier,
    submittingMessage: String? = null,
    checklist: @Composable () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigate by rememberUpdatedState(onNavigate)
    LaunchedEffect(effects, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collect { effect ->
                if (effect is OnboardingChecklistEffect.NavigateAfterCompletion) {
                    navigate(effect.destination)
                }
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
