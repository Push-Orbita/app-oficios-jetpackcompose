package com.cocido.oficios.feature.onboarding.gate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.InitialDestination
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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

    when (uiState) {
        OnboardingGateUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is OnboardingGateUiState.Error -> {
            val message = (uiState as OnboardingGateUiState.Error).message
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(OficiosSpacing.md),
                verticalArrangement = Arrangement.Center,
            ) {
                OficiosErrorState(
                    title = "No pudimos continuar",
                    description = message,
                    onRetry = viewModel::resolve,
                    modifier = Modifier.fillMaxWidth(),
                )
                OficiosButton(
                    text = "Ir a iniciar sesión",
                    onClick = onNavigateToLogin,
                    variant = OficiosButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        is OnboardingGateUiState.Ready -> Unit
    }
}
