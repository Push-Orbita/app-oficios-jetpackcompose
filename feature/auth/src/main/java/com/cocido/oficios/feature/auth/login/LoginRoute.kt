package com.cocido.oficios.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                LoginEffect.NavigateToRegister -> onNavigateToRegister()
                LoginEffect.NavigateToContactSupport -> onContactSupport()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
