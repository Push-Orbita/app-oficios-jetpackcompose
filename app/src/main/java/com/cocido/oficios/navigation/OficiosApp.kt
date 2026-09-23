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
import com.cocido.oficios.feature.home.navigation.HomeDestination
import com.cocido.oficios.feature.home.navigation.OrdersDestination
import com.cocido.oficios.feature.home.navigation.agendaScreen
import com.cocido.oficios.feature.home.navigation.homeScreen
import com.cocido.oficios.feature.home.navigation.ordersScreen
import com.cocido.oficios.feature.home.navigation.providerHomeScreen
import com.cocido.oficios.feature.home.navigation.sharedHomeScreens
import com.cocido.oficios.feature.onboarding.navigation.OnboardingDestination
import com.cocido.oficios.feature.onboarding.navigation.onboardingGraph
import com.cocido.oficios.feature.settings.navigation.SettingsDestination
import com.cocido.oficios.feature.settings.navigation.settingsScreen
import com.cocido.oficios.tracking.BookingTrackingService

@Composable
fun OficiosApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pendingPushRoute: String? = null,
    onPushRouteConsumed: () -> Unit = {},
) {
    LaunchedEffect(pendingPushRoute) {
        val route = pendingPushRoute ?: return@LaunchedEffect
        navController.currentBackStackEntryFlow.collect { entry ->
            val current = entry.destination.route ?: return@collect
            if (!canConsumePushFrom(current)) return@collect
            onPushRouteConsumed()
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
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
                navController.navigate(AuthDestination.REGISTER_TRADE) {
                    popUpTo(AuthDestination.REGISTER_PROVIDER) { inclusive = true }
                }
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
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                navController.navigate(
                    com.cocido.oficios.feature.onboarding.navigation.OnboardingDestination.verifyPhoneRoute(
                        contactId = contactId,
                        telefono = telefono,
                    ),
                ) {
                    // Reemplaza la pantalla de Contacto y domicilio en el back stack:
                    // su guardado pendiente ya lo maneja VerifyPhoneViewModel, así que
                    // "volver" desde la verificación debe ir al checklist, no repetir el formulario.
                    if (currentRoute != null) {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
            },
            onStepCompleted = {
                navController.popBackStack()
            },
            onLoggedOut = {
                BookingTrackingService.stop(context)
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
        providerHomeScreen(
            navController = navController,
            onNavigateToSettings = {
                navController.navigate(SettingsDestination.route)
            },
            onNavigateToCompleteProfile = {
                navController.navigate(OnboardingDestination.PROVIDER_CHECKLIST) {
                    launchSingleTop = true
                }
            },
            onShowMessage = showMessage,
        )
        ordersScreen(
            navController = navController,
            onShowMessage = showMessage,
            onStartTracking = { bookingId -> BookingTrackingService.start(context, bookingId) },
            onStopTracking = { BookingTrackingService.stop(context) },
        )
        agendaScreen(
            navController = navController,
            onShowMessage = showMessage,
        )
        settingsScreen(
            navController = navController,
            onNavigateBack = { navController.popBackStack() },
            onLoggedOut = {
                BookingTrackingService.stop(context)
                navController.navigateToLoginClearingBackStack()
            },
            onShowMessage = showMessage,
            onNavigateToVerifyPhone = { contactId, telefono ->
                navController.navigate(OnboardingDestination.verifyPhoneRoute(contactId, telefono))
            },
        )
        sharedHomeScreens(
            navController = navController,
            onShowMessage = showMessage,
            onNavigateToWorker = { workerId ->
                navController.navigate(HomeDestination.workerProfile(workerId)) {
                    launchSingleTop = true
                }
            },
            onNavigateToHire = { workerId ->
                navController.navigate(OrdersDestination.createOrder(workerId)) {
                    launchSingleTop = true
                }
            },
        )
    }
}

private fun canConsumePushFrom(currentRoute: String): Boolean = when {
    currentRoute == AuthDestination.SPLASH -> false
    currentRoute == AuthDestination.LOGIN -> false
    currentRoute == AuthDestination.FORGOT_PASSWORD -> false
    currentRoute == AuthDestination.RESET_PASSWORD -> false
    currentRoute.startsWith("register/") -> false
    else -> true
}
