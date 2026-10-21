package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.home.agenda.AgendaConfigRoute
import com.cocido.oficios.feature.home.agenda.AgendaListRoute
import com.cocido.oficios.feature.home.agenda.BookingsAgendaRoute

/** Agenda del trabajador — alcanzable solo desde Home trabajador, pero registrada como
 * ruta de nivel superior por consistencia con el resto del NavHost (ver [ordersScreen]). */
fun NavGraphBuilder.agendaScreen(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
) {
    composable(AgendaDestination.list) {
        BookingsAgendaRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSlots = { navController.navigate(AgendaDestination.slots) },
            onNavigateToOrderDetail = { orderId ->
                navController.navigate(OrdersDestination.orderDetail(orderId))
            },
            onShowMessage = onShowMessage,
        )
    }

    composable(AgendaDestination.slots) {
        AgendaListRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToConfig = { navController.navigate(AgendaDestination.config) },
            onShowMessage = onShowMessage,
        )
    }

    composable(AgendaDestination.config) {
        AgendaConfigRoute(
            onNavigateBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }
}
