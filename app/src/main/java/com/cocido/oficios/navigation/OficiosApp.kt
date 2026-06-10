package com.cocido.oficios.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cocido.oficios.feature.auth.navigation.AuthDestination
import com.cocido.oficios.feature.auth.navigation.authGraph
import com.cocido.oficios.feature.home.navigation.HomeDestination
import com.cocido.oficios.feature.home.navigation.homeScreen
import com.cocido.oficios.feature.onboarding.navigation.onboardingGraph
import com.cocido.oficios.feature.settings.navigation.SettingsDestination
import com.cocido.oficios.feature.settings.navigation.settingsScreen

@Composable
fun OficiosApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
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
            onNavigateToRegisterProvider = {
                navController.navigate(AuthDestination.REGISTER_PROVIDER)
            },
            onNavigateToForgotPassword = {
                navController.navigate(AuthDestination.FORGOT_PASSWORD)
            },
            onNavigateToResetPassword = {
                navController.navigate(AuthDestination.RESET_PASSWORD)
            },
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
            onNavigateToClientHome = {
                navController.navigate(HomeDestination.route) {
                    popUpTo(AuthDestination.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToProviderHome = {
                navController.navigate(HomeDestination.route) {
                    popUpTo(AuthDestination.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onLogout = {
                navController.navigateToLoginClearingBackStack()
            },
        )
        homeScreen(
            onNavigateToSettings = {
                navController.navigate(SettingsDestination.route)
            },
        )
        settingsScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
