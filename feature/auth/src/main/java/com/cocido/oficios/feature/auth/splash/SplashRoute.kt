package com.cocido.oficios.feature.auth.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.domain.model.InitialDestination

@Composable
fun SplashRoute(
    onNavigate: (InitialDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.Ready) {
            onNavigate((uiState as SplashUiState.Ready).destination)
        }
    }

    SplashScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}
