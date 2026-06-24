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
import com.cocido.oficios.feature.onboarding.steps.ClientContactoRoute
import com.cocido.oficios.feature.onboarding.steps.ClientDomicilioRoute
import com.cocido.oficios.feature.onboarding.steps.ProviderAvailabilityRoute
import com.cocido.oficios.feature.onboarding.steps.ProviderDniRoute
import com.cocido.oficios.feature.onboarding.steps.ProviderPaymentMethodsRoute
import com.cocido.oficios.feature.onboarding.steps.ProviderLocalRoute
import com.cocido.oficios.feature.onboarding.steps.ProviderProfessionalInfoRoute
import com.cocido.oficios.feature.onboarding.steps.ProviderRubrosRoute
import com.cocido.oficios.feature.onboarding.verify.VerifyEmailRoute

fun NavGraphBuilder.onboardingGraph(
    onNavigateFromGate: (InitialDestination) -> Unit,
    onNavigateToProviderOnboarding: () -> Unit,
    onNavigateToClientHome: () -> Unit,
    onNavigateToProviderHome: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    onStepCompleted: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    composable(OnboardingDestination.GATE) {
        OnboardingGateRoute(
            onNavigate = onNavigateFromGate,
            onNavigateToLogin = onLoggedOut,
        )
    }
    composable(OnboardingDestination.CLIENT_CHECKLIST) {
        ClientOnboardingRoute(
            onLoggedOut = onLoggedOut,
            onNavigateToStep = onNavigateToStep,
            onNavigateFromGate = onNavigateFromGate,
            onRefresh = onStepCompleted,
        )
    }
    composable(OnboardingDestination.PROVIDER_CHECKLIST) {
        ProviderOnboardingRoute(
            onLoggedOut = onLoggedOut,
            onNavigateToStep = onNavigateToStep,
            onNavigateFromGate = onNavigateFromGate,
            onRefresh = onStepCompleted,
        )
    }
    composable(OnboardingDestination.PROVIDER_PENDING) {
        ProviderPendingRoute(onLoggedOut = onLoggedOut)
    }
    composable(OnboardingDestination.PROVIDER_REJECTED) {
        ProviderRejectedRoute(
            onFixProfile = onNavigateToProviderOnboarding,
            onLoggedOut = onLoggedOut,
            onNavigateToStep = onNavigateToStep,
            onRefresh = onStepCompleted,
        )
    }
    composable(OnboardingDestination.CLIENT_BLOCKED) {
        ClientBlockedRoute(onLoggedOut = onLoggedOut)
    }
    composable(OnboardingDestination.CLIENT_INACTIVE) {
        ClientInactiveRoute(onLoggedOut = onLoggedOut)
    }
    composable(OnboardingDestination.HOME_CLIENT) {
        ClientHomeShellRoute(
            onContinue = onNavigateToClientHome,
            onLoggedOut = onLoggedOut,
        )
    }
    composable(OnboardingDestination.HOME_PROVIDER) {
        ProviderHomeShellRoute(
            onContinue = onNavigateToProviderHome,
            onLoggedOut = onLoggedOut,
        )
    }
    composable(OnboardingDestination.VERIFY_EMAIL) {
        VerifyEmailRoute(onVerified = onStepCompleted)
    }
    composable(OnboardingDestination.CLIENT_DOMICILIO) {
        ClientDomicilioRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.CLIENT_CONTACTO) {
        ClientContactoRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.PROVIDER_DNI) {
        ProviderDniRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.PROVIDER_PROFESSIONAL_INFO) {
        ProviderProfessionalInfoRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.PROVIDER_RUBROS) {
        ProviderRubrosRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.PROVIDER_AVAILABILITY) {
        ProviderAvailabilityRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.PROVIDER_PAYMENT_METHODS) {
        ProviderPaymentMethodsRoute(onSaved = onStepCompleted)
    }
    composable(OnboardingDestination.PROVIDER_LOCAL) {
        ProviderLocalRoute(onSaved = onStepCompleted)
    }
}
