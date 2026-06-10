package com.cocido.oficios.navigation

import androidx.navigation.NavHostController
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.feature.auth.navigation.AuthDestination
import com.cocido.oficios.feature.onboarding.navigation.OnboardingDestination

fun NavHostController.navigateToInitialDestination(destination: InitialDestination) {
    val route = when (destination) {
        InitialDestination.Login -> AuthDestination.LOGIN
        InitialDestination.ClientOnboarding -> OnboardingDestination.CLIENT_CHECKLIST
        InitialDestination.ProviderOnboarding -> OnboardingDestination.PROVIDER_CHECKLIST
        InitialDestination.ProviderPendingApproval -> OnboardingDestination.PROVIDER_PENDING
        InitialDestination.ProviderRejected -> OnboardingDestination.PROVIDER_REJECTED
        InitialDestination.ClientHome -> OnboardingDestination.HOME_CLIENT
        InitialDestination.ProviderHome -> OnboardingDestination.HOME_PROVIDER
        InitialDestination.ClientBlocked -> OnboardingDestination.CLIENT_BLOCKED
        InitialDestination.ClientInactive -> OnboardingDestination.CLIENT_INACTIVE
    }
    navigate(route) {
        popUpTo(AuthDestination.SPLASH) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToOnboardingGate() {
    navigate(OnboardingDestination.GATE) {
        popUpTo(AuthDestination.SPLASH) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToLoginClearingBackStack() {
    navigate(AuthDestination.LOGIN) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
