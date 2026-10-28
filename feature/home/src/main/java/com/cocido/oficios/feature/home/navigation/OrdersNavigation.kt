package com.cocido.oficios.feature.home.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cocido.oficios.feature.home.orders.CounterOfferRoute
import com.cocido.oficios.feature.home.orders.CreateOrderRoute
import com.cocido.oficios.feature.home.orders.MisUbicacionesRoute
import com.cocido.oficios.feature.home.orders.OrderDetailRoute
import com.cocido.oficios.feature.home.orders.OrdersListRoute
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.navigation.HomeDestination
import com.cocido.oficios.feature.home.orders.RateWorkerRoute

/**
 * Pantallas del módulo de Pedidos — alcanzables tanto desde el graph de Home cliente como
 * desde el de Home trabajador (una solicitud involucra a los dos roles), así que se registran
 * como rutas de nivel superior en el NavHost en vez de anidarlas en un solo graph por rol.
 */
fun NavGraphBuilder.ordersScreen(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
    onStartTracking: (Long) -> Unit,
    onStopTracking: () -> Unit,
) {
    composable(
        route = OrdersDestination.createOrder,
        arguments = listOf(navArgument("workerId") { type = NavType.LongType }),
    ) { entry ->
        val pickedLocationId by entry.savedStateHandle
            .getStateFlow(SELECTED_DOMICILIO_KEY, -1L)
            .collectAsState()
        CreateOrderRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateHome = {
                if (!navController.popBackStack(HomeDestination.inicio, inclusive = false)) {
                    navController.navigate(HomeDestination.inicio) {
                        popUpTo(OrdersDestination.createOrder) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            onNavigateToAddLocation = { selectedId ->
                navController.navigate(OrdersDestination.locations(selectedId))
            },
            pickedLocationId = pickedLocationId.takeIf { it > 0L },
            onPickedLocationConsumed = { entry.savedStateHandle.remove<Long>(SELECTED_DOMICILIO_KEY) },
            onShowMessage = onShowMessage,
            onTabSelected = { navController.openClientTab(it, settingsRoute = "settings") },
        )
    }

    composable(
        route = OrdersDestination.locations,
        arguments = listOf(navArgument("selectedId") { type = NavType.LongType; defaultValue = -1L }),
    ) {
        MisUbicacionesRoute(
            onNavigateBack = { navController.popBackStack() },
            onConfirmed = { selectedId ->
                navController.previousBackStackEntry?.savedStateHandle?.set(SELECTED_DOMICILIO_KEY, selectedId)
                navController.popBackStack()
            },
            onShowMessage = onShowMessage,
            onTabSelected = { navController.openClientTab(it, settingsRoute = "settings") },
        )
    }

    composable(
        route = OrdersDestination.orderDetail,
        arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
    ) {
        OrderDetailRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToWorker = { workerId -> navController.navigate(HomeDestination.workerProfile(workerId)) },
            onNavigateToCounterOffer = { orderId -> navController.navigate(OrdersDestination.counterOffer(orderId)) },
            onNavigateToRate = { bookingId, _, workerName ->
                navController.navigate(OrdersDestination.rateWorker(bookingId, workerName))
            },
            onShowMessage = onShowMessage,
            onStartTracking = onStartTracking,
            onStopTracking = onStopTracking,
        )
    }

    composable(
        route = OrdersDestination.counterOffer,
        arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
    ) {
        CounterOfferRoute(
            onNavigateBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }

    composable(
        route = OrdersDestination.rateWorker,
        arguments = listOf(
            navArgument("bookingId") { type = NavType.LongType },
            navArgument("workerName") { type = NavType.StringType; defaultValue = "" },
        ),
    ) {
        RateWorkerRoute(
            onNavigateBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }

    composable(OrdersDestination.ordersList) {
        OrdersListRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToOrderDetail = { orderId -> navController.navigate(OrdersDestination.orderDetail(orderId)) },
            onNavigateToSearch = { navController.openClientTab(ClientHomeTab.INICIO, settingsRoute = "settings") },
            showBackNavigation = false,
            onTabSelected = { navController.openClientTab(it, settingsRoute = "settings") },
        )
    }
}

private const val SELECTED_DOMICILIO_KEY = "selectedDomicilioId"
