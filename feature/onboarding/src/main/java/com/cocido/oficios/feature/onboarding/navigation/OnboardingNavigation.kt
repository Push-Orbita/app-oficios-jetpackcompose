package com.cocido.oficios.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.feature.onboarding.ClientBlockedRoute
import com.cocido.oficios.feature.onboarding.ClientHomeShellRoute
import com.cocido.oficios.feature.onboarding.ClientInactiveRoute
import com.cocido.oficios.feature.onboarding.ClientOnboardingRoute
import com.cocido.oficios.feature.onboarding.ProviderHomeShellRoute
import com.cocido.oficios.feature.onboarding.ProviderOnboardingRoute
import com.cocido.oficios.feature.onboarding.ProviderPendingRoute
import com.cocido.oficios.feature.onboarding.ProviderRejectedRoute
import com.cocido.oficios.feature.onboarding.gate.OnboardingGateRoute

fun NavGraphBuilder.onboardingGraph(
    onNavigateFromGate: (InitialDestination) -> Unit,
    onNavigateToProviderOnboarding: () -> Unit,
    onNavigateToClientHome: () -> Unit,
    onNavigateToProviderHome: () -> Unit,
    onLogout: () -> Unit,
) {
    composable(OnboardingDestination.GATE) {
        OnboardingGateRoute(onNavigate = onNavigateFromGate)
    }
    composable(OnboardingDestination.CLIENT_CHECKLIST) {
        ClientOnboardingRoute(onLogout = onLogout)
    }
    composable(OnboardingDestination.PROVIDER_CHECKLIST) {
        ProviderOnboardingRoute(onLogout = onLogout)
    }
    composable(OnboardingDestination.PROVIDER_PENDING) {
        ProviderPendingRoute(onLogout = onLogout)
    }
    composable(OnboardingDestination.PROVIDER_REJECTED) {
        ProviderRejectedRoute(
            onFixProfile = onNavigateToProviderOnboarding,
            onLogout = onLogout,
        )
    }
    composable(OnboardingDestination.CLIENT_BLOCKED) {
        ClientBlockedRoute(onLogout = onLogout)
    }
    composable(OnboardingDestination.CLIENT_INACTIVE) {
        ClientInactiveRoute(onLogout = onLogout)
    }
    composable(OnboardingDestination.HOME_CLIENT) {
        ClientHomeShellRoute(
            onContinue = onNavigateToClientHome,
            onLogout = onLogout,
        )
    }
    composable(OnboardingDestination.HOME_PROVIDER) {
        ProviderHomeShellRoute(
            onContinue = onNavigateToProviderHome,
            onLogout = onLogout,
        )
    }
}
