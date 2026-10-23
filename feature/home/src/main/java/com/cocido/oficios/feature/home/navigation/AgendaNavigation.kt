package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cocido.oficios.feature.home.agenda.AgendaConfigRoute
import com.cocido.oficios.feature.home.agenda.AgendaListRoute
import com.cocido.oficios.feature.home.agenda.BookingsAgendaRoute
import com.cocido.oficios.feature.home.agenda.availability.AgendaBlocksRoute
import com.cocido.oficios.feature.home.agenda.availability.AvailabilityHubRoute
import com.cocido.oficios.feature.home.agenda.availability.WeeklyAvailabilityRoute

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
            onNavigateToAvailability = { navController.navigate(AgendaDestination.availability) },
        )
    }
    composable(AgendaDestination.availability) {
        AvailabilityHubRoute(
            onNavigateBack = { navController.popBackStack() },
            onOpenWeek = { subRubroId -> navController.navigate(AgendaDestination.weekly(subRubroId)) },
            onOpenBlocks = { subRubroId -> navController.navigate(AgendaDestination.blocks(subRubroId)) },
        )
    }
    composable(
        route = AgendaDestination.weekly,
        arguments = listOf(navArgument("subRubroId") { type = NavType.LongType }),
    ) {
        WeeklyAvailabilityRoute(
            onNavigateBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }
    composable(
        route = AgendaDestination.blocks,
        arguments = listOf(navArgument("subRubroId") { type = NavType.LongType }),
    ) {
        AgendaBlocksRoute(
            onNavigateBack = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }
}
