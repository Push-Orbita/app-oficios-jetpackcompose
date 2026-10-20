package com.cocido.oficios.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosSnackbarController
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSnackbarController
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSnackbarHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cocido.oficios.core.ui.intent.openSupportPhone
import com.cocido.oficios.core.ui.intent.openTermsAndConditions
import com.cocido.oficios.core.ui.legal.TermsScreen
import com.cocido.oficios.feature.auth.navigation.AuthDestination
import com.cocido.oficios.feature.auth.navigation.authGraph
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.navigation.HomeDestination
import com.cocido.oficios.feature.home.navigation.OrdersDestination
import com.cocido.oficios.feature.home.navigation.SharedHomeDestination
import com.cocido.oficios.feature.home.navigation.agendaScreen
import com.cocido.oficios.feature.home.navigation.homeScreen
import com.cocido.oficios.feature.home.navigation.openClientTab
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
    val snackbarController = remember { OficiosSnackbarController() }
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
    CompositionLocalProvider(LocalOficiosSnackbarController provides snackbarController) {
        Box(modifier = modifier.fillMaxSize()) {
            AppNavHost(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
            )
            OficiosSnackbarHost(
                controller = snackbarController,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AuthDestination.SPLASH,
) {
    val context = LocalContext.current
    val snackbar = LocalOficiosSnackbarController.current
    val showMessage: (String) -> Unit = { message -> snackbar.show(message) }
    val transitions = rememberOficiosNavTransitions()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
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
            onNavigateToTerms = {
                openTermsAndConditions(context) { navController.navigate(AppRoutes.TERMS) }
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
                navController.openClientTab(ClientHomeTab.AJUSTES, SettingsDestination.route)
            },
            onShowMessage = showMessage,
        )
        providerHomeScreen(
            navController = navController,
            onNavigateToSettings = {
                navController.openClientTab(ClientHomeTab.AJUSTES, SettingsDestination.route)
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
            onNavigateToTerms = {
                openTermsAndConditions(context) { navController.navigate(AppRoutes.TERMS) }
            },
            onNavigateToHelp = {
                navController.navigate(SharedHomeDestination.help)
            },
            onNavigateToVerifyPhone = { contactId, telefono ->
                navController.navigate(OnboardingDestination.verifyPhoneRoute(contactId, telefono))
            },
            bottomBar = {
                ClientHomeBottomBar(
                    selectedTab = com.cocido.oficios.feature.home.common.ClientHomeTabNavigator.tabForDestination(
                        SettingsDestination.route,
                    ),
                    onTabSelected = { navController.openClientTab(it, SettingsDestination.route) },
                )
            },
        )
        composable(AppRoutes.TERMS) {
            TermsScreen(onBack = { navController.popBackStack() })
        }
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
            onNavigateToSearch = {
                navController.navigate(HomeDestination.categories) {
                    launchSingleTop = true
                }
            },
            settingsRoute = SettingsDestination.route,
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
