package com.cocido.oficios.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.onboarding.client.ClientOnboardingViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderOnboardingViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderPendingViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderRejectedViewModel

@Composable
fun ClientOnboardingRoute(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientOnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClientOnboardingChecklistScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ProviderOnboardingRoute(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderOnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProviderOnboardingChecklistScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ProviderPendingRoute(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderPendingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProviderPendingApprovalScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ProviderRejectedRoute(
    onFixProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderRejectedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProviderRejectedScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onFixProfile = onFixProfile,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ClientBlockedRoute(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AccountStatusScreen(
        title = stringResource(R.string.onboarding_blocked_title),
        subtitle = stringResource(R.string.onboarding_blocked_subtitle),
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ClientInactiveRoute(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AccountStatusScreen(
        title = stringResource(R.string.onboarding_inactive_title),
        subtitle = stringResource(R.string.onboarding_inactive_subtitle),
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ClientHomeShellRoute(
    onContinue: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeShellScreen(
        title = stringResource(R.string.onboarding_go_home_client),
        subtitle = "Bienvenido a Oficios",
        onContinue = onContinue,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
fun ProviderHomeShellRoute(
    onContinue: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeShellScreen(
        title = stringResource(R.string.onboarding_go_home_provider),
        subtitle = "Tu perfil está listo para operar",
        onContinue = onContinue,
        onLogout = onLogout,
        modifier = modifier,
    )
}
