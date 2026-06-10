package com.cocido.oficios.feature.auth.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegisterRoleSelectionRoute(
    onSelectClient: () -> Unit,
    onSelectProvider: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    RegisterRoleSelectionScreen(
        onSelectClient = onSelectClient,
        onSelectProvider = onSelectProvider,
        onNavigateToLogin = onNavigateToLogin,
        onContactSupport = onContactSupport,
        modifier = modifier,
    )
}

@Composable
fun RegisterClientRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    RegisterFormRoute(
        mode = RegisterMode.CLIENT,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
fun RegisterProviderRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    RegisterFormRoute(
        mode = RegisterMode.PROVIDER,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
private fun RegisterFormRoute(
    mode: RegisterMode,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mode) {
        viewModel.setMode(mode)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                RegisterEffect.NavigateToTerms -> Unit
            }
        }
    }

    RegisterFormScreen(
        mode = mode,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
