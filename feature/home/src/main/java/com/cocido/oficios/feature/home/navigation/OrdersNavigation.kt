package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cocido.oficios.feature.home.orders.CounterOfferRoute
import com.cocido.oficios.feature.home.orders.CreateOrderRoute
import com.cocido.oficios.feature.home.orders.OrderDetailRoute
import com.cocido.oficios.feature.home.orders.OrdersListRoute
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
    ) {
        CreateOrderRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToOrderDetail = { orderId ->
                navController.navigate(OrdersDestination.orderDetail(orderId)) {
                    popUpTo(OrdersDestination.createOrder) { inclusive = true }
                }
            },
            onShowMessage = onShowMessage,
        )
    }

    composable(
        route = OrdersDestination.orderDetail,
        arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
    ) {
        OrderDetailRoute(
            onNavigateBack = { navController.popBackStack() },
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
            showBackNavigation = false,
            onTabSelected = { navController.openClientTab(it, settingsRoute = "settings") },
        )
    }
}
