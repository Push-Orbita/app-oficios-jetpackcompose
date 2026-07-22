package com.cocido.oficios.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cocido.oficios.core.ui.intent.openSupportPhone
import com.cocido.oficios.core.ui.intent.openTermsAndConditions
import com.cocido.oficios.feature.auth.navigation.AuthDestination
import com.cocido.oficios.feature.auth.navigation.authGraph
import com.cocido.oficios.feature.home.navigation.homeScreen
import com.cocido.oficios.feature.onboarding.navigation.onboardingGraph
import com.cocido.oficios.feature.settings.navigation.SettingsDestination
import com.cocido.oficios.feature.settings.navigation.settingsScreen

@Composable
fun OficiosApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pendingPushRoute: String? = null,
    onPushRouteConsumed: () -> Unit = {},
) {
    LaunchedEffect(pendingPushRoute) {
        val route = pendingPushRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            launchSingleTop = true
        }
        onPushRouteConsumed()
    }
    AppNavHost(
        navController = navController,
        modifier = modifier,
    )
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AuthDestination.SPLASH,
) {
    val context = LocalContext.current
    val showMessage: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authGraph(
            onNavigateFromSplash = { destination ->
                navController.navigateToInitialDestination(destination)
            },
            onNavigateToOnboarding = {
                navController.navigateToOnboardingGate()
            },
            onNavigateToLogin = {
                navController.navigate(AuthDestination.LOGIN) {
                    launchSingleTop = true
                }
            },
            onNavigateToRegisterRole = {
                navController.navigate(AuthDestination.REGISTER_ROLE)
            },
            onNavigateToRegisterClient = {
                navController.navigate(AuthDestination.REGISTER_CLIENT)
            },
            onNavigateToRegisterTrade = {
                navController.navigate(AuthDestination.REGISTER_TRADE)
            },
            onNavigateToRegisterProvider = {
                navController.navigate(AuthDestination.REGISTER_PROVIDER)
            },
            onNavigateToForgotPassword = {
                navController.navigate(AuthDestination.FORGOT_PASSWORD)
            },
            onNavigateToResetPassword = {
                navController.navigate(AuthDestination.RESET_PASSWORD)
            },
            onContactSupport = { openSupportPhone(context) },
            onNavigateToTerms = { openTermsAndConditions(context) },
        )
        onboardingGraph(
            onNavigateFromGate = { destination ->
                navController.navigateToInitialDestination(destination)
            },
            onNavigateToProviderOnboarding = {
                navController.navigate(com.cocido.oficios.feature.onboarding.navigation.OnboardingDestination.PROVIDER_CHECKLIST) {
                    launchSingleTop = true
                }
            },
            onNavigateToStep = { route ->
                navController.navigate(route)
            },
            onNavigateToVerifyPhone = { contactId, telefono ->
                navController.navigate(
                    com.cocido.oficios.feature.onboarding.navigation.OnboardingDestination.verifyPhoneRoute(
                        contactId = contactId,
                        telefono = telefono,
                    ),
                )
            },
            onStepCompleted = {
                navController.popBackStack()
            },
            onLoggedOut = {
                navController.navigateToLoginClearingBackStack()
            },
        )
        homeScreen(
            navController = navController,
            onNavigateToSettings = {
                navController.navigate(SettingsDestination.route)
            },
            onShowMessage = showMessage,
        )
        settingsScreen(
            onNavigateBack = { navController.popBackStack() },
            onLoggedOut = { navController.navigateToLoginClearingBackStack() },
        )
    }
}
