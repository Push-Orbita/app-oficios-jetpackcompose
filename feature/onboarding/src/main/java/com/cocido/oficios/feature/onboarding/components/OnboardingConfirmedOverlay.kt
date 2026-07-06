package com.cocido.oficios.feature.onboarding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.feedback.OficiosAnimatedSuccessOverlay
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.feature.onboarding.R

@Composable
fun OnboardingConfirmedOverlay(
    destination: InitialDestination,
    modifier: Modifier = Modifier,
) {
    val (title, description) = when (destination) {
        InitialDestination.ClientHome -> stringResource(
            R.string.onboarding_client_confirmed_home_title,
        ) to stringResource(R.string.onboarding_client_confirmed_home_subtitle)
        InitialDestination.ProviderHome -> stringResource(
            R.string.onboarding_provider_confirmed_home_title,
        ) to stringResource(R.string.onboarding_provider_confirmed_home_subtitle)
        InitialDestination.ProviderPendingApproval -> stringResource(
            R.string.onboarding_provider_confirmed_pending_title,
        ) to stringResource(R.string.onboarding_provider_confirmed_pending_subtitle)
        else -> stringResource(
            R.string.onboarding_confirmed_default_title,
        ) to stringResource(R.string.onboarding_confirmed_default_subtitle)
    }

    OficiosAnimatedSuccessOverlay(
        title = title,
        description = description,
        modifier = modifier,
    )
}
