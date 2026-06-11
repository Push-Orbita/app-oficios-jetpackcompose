package com.cocido.oficios.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.feature.onboarding.client.ClientOnboardingViewModel
import com.cocido.oficios.feature.onboarding.navigation.OnboardingStepNavigator
import com.cocido.oficios.feature.onboarding.provider.ProviderOnboardingViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderPendingViewModel
import com.cocido.oficios.feature.onboarding.provider.ProviderRejectedViewModel
import com.cocido.oficios.feature.onboarding.session.LogoutViewModel
import com.cocido.oficios.feature.onboarding.session.OnboardingLogoutEffect

@Composable
fun ClientOnboardingRoute(
    onLoggedOut: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientOnboardingViewModel = hiltViewModel(),
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    ReloadOnResume { viewModel.load() }

    ClientOnboardingChecklistScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogout = logoutViewModel::logout,
        onStepClick = { step -> navigateToStep(step, onNavigateToStep) },
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

@Composable
fun ProviderOnboardingRoute(
    onLoggedOut: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    onNavigateFromGate: (InitialDestination) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderOnboardingViewModel = hiltViewModel(),
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()
    val isFinalizing by viewModel.isFinalizing.collectAsStateWithLifecycle()

    ReloadOnResume { viewModel.load() }

    ProviderOnboardingChecklistScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogout = logoutViewModel::logout,
        onStepClick = { step -> navigateToStep(step, onNavigateToStep) },
        onFinalizeProfile = { viewModel.finalizeProfile { onNavigateFromGate(InitialDestination.ProviderHome) } },
        isLoggingOut = isLoggingOut,
        isFinalizing = isFinalizing,
        modifier = modifier,
    )
}

@Composable
fun ProviderPendingRoute(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderPendingViewModel = hiltViewModel(),
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    ProviderPendingApprovalScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onLogout = logoutViewModel::logout,
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

@Composable
fun ProviderRejectedRoute(
    onFixProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderRejectedViewModel = hiltViewModel(),
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    ReloadOnResume { viewModel.load() }

    ProviderRejectedScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onFixProfile = onFixProfile,
        onLogout = logoutViewModel::logout,
        onStepClick = { step -> navigateToStep(step, onNavigateToStep) },
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

private fun navigateToStep(step: OnboardingStep, onNavigateToStep: (String) -> Unit) {
    OnboardingStepNavigator.routeForStep(step.id)?.let(onNavigateToStep)
}

@Composable
private fun ReloadOnResume(onReload: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onReload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
fun ClientBlockedRoute(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    AccountStatusScreen(
        title = stringResource(R.string.onboarding_blocked_title),
        subtitle = stringResource(R.string.onboarding_blocked_subtitle),
        onLogout = logoutViewModel::logout,
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

@Composable
fun ClientInactiveRoute(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    AccountStatusScreen(
        title = stringResource(R.string.onboarding_inactive_title),
        subtitle = stringResource(R.string.onboarding_inactive_subtitle),
        onLogout = logoutViewModel::logout,
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

@Composable
fun ClientHomeShellRoute(
    onContinue: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    HomeShellScreen(
        title = stringResource(R.string.onboarding_go_home_client),
        subtitle = stringResource(R.string.onboarding_home_client_subtitle),
        onContinue = onContinue,
        onLogout = logoutViewModel::logout,
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

@Composable
fun ProviderHomeShellRoute(
    onContinue: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    logoutViewModel: LogoutViewModel = hiltViewModel(),
) {
    OnboardingLogoutEffect(onLoggedOut = onLoggedOut, logoutViewModel = logoutViewModel)
    val isLoggingOut by logoutViewModel.isLoading.collectAsStateWithLifecycle()

    HomeShellScreen(
        title = stringResource(R.string.onboarding_go_home_provider),
        subtitle = stringResource(R.string.onboarding_home_provider_subtitle),
        onContinue = onContinue,
        onLogout = logoutViewModel::logout,
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}
