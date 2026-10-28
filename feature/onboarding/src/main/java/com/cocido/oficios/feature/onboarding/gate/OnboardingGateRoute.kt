package com.cocido.oficios.feature.onboarding.gate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.oficios.feature.onboarding.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.InitialDestination
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

@Composable
fun OnboardingGateRoute(
    onNavigate: (InitialDestination) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingGateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is OnboardingGateUiState.Ready) {
            onNavigate((uiState as OnboardingGateUiState.Ready).destination)
        }
    }

    OnboardingGateScreen(
        uiState = uiState,
        onRetry = viewModel::resolve,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

@Composable
fun OnboardingGateScreen(
    uiState: OnboardingGateUiState,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosStateContent(targetState = uiState, modifier = modifier) { state ->
        when (state) {
            OnboardingGateUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    OficiosProgress()
                }
            }
            is OnboardingGateUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(OficiosSpacing.md),
                    verticalArrangement = Arrangement.Center,
                ) {
                    OficiosErrorState(
                        title = stringResource(R.string.onboarding_gate_error_title),
                        description = state.message,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OficiosButton(
                        text = stringResource(R.string.onboarding_gate_go_login),
                        onClick = onNavigateToLogin,
                        variant = OficiosButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            is OnboardingGateUiState.Ready -> Unit
        }
    }
}
