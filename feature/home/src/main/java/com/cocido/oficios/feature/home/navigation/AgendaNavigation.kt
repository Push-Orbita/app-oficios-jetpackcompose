package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.home.agenda.AgendaConfigRoute
import com.cocido.oficios.feature.home.agenda.AgendaListRoute

/** Agenda del trabajador — alcanzable solo desde Home trabajador, pero registrada como
 * ruta de nivel superior por consistencia con el resto del NavHost (ver [ordersScreen]). */
fun NavGraphBuilder.agendaScreen(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
) {
    composable(AgendaDestination.list) {
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
