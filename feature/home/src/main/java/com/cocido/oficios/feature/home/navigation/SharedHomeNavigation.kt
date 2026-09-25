package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.favorites.FavoritesRoute
import com.cocido.oficios.feature.home.help.HelpRoute
import com.cocido.oficios.feature.home.notifications.NotificationsRoute

fun NavGraphBuilder.sharedHomeScreens(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
    onNavigateToWorker: (Long) -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    settingsRoute: String,
) {
    composable(SharedHomeDestination.help) {
        HelpRoute(
            onNavigateBack = { navController.popBackStack() },
            showBackNavigation = false,
            onTabSelected = { navController.openClientTab(it, settingsRoute) },
        )
    }
    composable(SharedHomeDestination.favorites) {
        FavoritesRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToWorker = onNavigateToWorker,
            onNavigateToHire = onNavigateToHire,
            onNavigateToSearch = onNavigateToSearch,
            showBackNavigation = false,
            onTabSelected = { navController.openClientTab(it, settingsRoute) },
        )
    }
    composable(SharedHomeDestination.notifications) {
        NotificationsRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDeepLink = { route ->
                navController.navigate(route) {
                    launchSingleTop = true
                }
            },
            onShowMessage = onShowMessage,
        )
    }
}

fun NavHostController.navigateToHelp() {
    openClientTab(ClientHomeTab.AYUDA, settingsRoute = "settings")
}

fun NavHostController.navigateToFavorites() {
    openClientTab(ClientHomeTab.FAVORITOS, settingsRoute = "settings")
}

fun NavHostController.openClientTab(tab: ClientHomeTab, settingsRoute: String) {
    val anchor = clientTabAnchor()
    navigate(clientTabRoute(tab, anchor, settingsRoute)) {
        popUpTo(anchor) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun clientTabRoute(tab: ClientHomeTab, anchor: String, settingsRoute: String): String = when (tab) {
    ClientHomeTab.INICIO -> anchor
    ClientHomeTab.PEDIDOS -> OrdersDestination.ordersList
    ClientHomeTab.FAVORITOS -> SharedHomeDestination.favorites
    ClientHomeTab.AYUDA -> SharedHomeDestination.help
    ClientHomeTab.AJUSTES -> settingsRoute
}

private fun NavHostController.clientTabAnchor(): String =
    if (isOnBackStack(ProviderHomeDestination.inicio)) {
        ProviderHomeDestination.inicio
    } else {
        HomeDestination.inicio
    }

private fun NavHostController.isOnBackStack(route: String): Boolean =
    try {
        getBackStackEntry(route)
        true
    } catch (_: IllegalArgumentException) {
        false
    }

fun NavHostController.navigateToNotifications() {
    navigate(SharedHomeDestination.notifications) {
        launchSingleTop = true
    }
}
