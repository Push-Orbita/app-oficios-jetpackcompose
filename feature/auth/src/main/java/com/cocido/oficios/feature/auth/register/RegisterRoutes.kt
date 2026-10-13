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
fun RegisterTradeRoute(
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterTradeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RegisterTradeEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    RegisterTradeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun RegisterClientRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    RegisterFormRoute(
        mode = RegisterMode.CLIENT,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToTerms = onNavigateToTerms,
        onContactSupport = onContactSupport,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
fun RegisterProviderRoute(
    onNavigateToTradeSetup: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    RegisterFormRoute(
        mode = RegisterMode.PROVIDER,
        onNavigateToOnboarding = onNavigateToOnboarding,
        onNavigateToTradeSetup = onNavigateToTradeSetup,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToTerms = onNavigateToTerms,
        onContactSupport = onContactSupport,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
private fun RegisterFormRoute(
    mode: RegisterMode,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToTradeSetup: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onContactSupport: () -> Unit,
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
                RegisterEffect.NavigateToTradeSetup -> onNavigateToTradeSetup()
                RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                RegisterEffect.NavigateToTerms -> onNavigateToTerms()
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
