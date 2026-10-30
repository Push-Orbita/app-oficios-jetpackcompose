package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.home.common.ClientHomeTabNavigator
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
            showBackNavigation = true,
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
    navigate(SharedHomeDestination.help) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToFavorites() {
    openClientTab(ClientHomeTab.FAVORITOS, settingsRoute = "settings")
}

fun NavHostController.openClientTab(tab: ClientHomeTab, settingsRoute: String) {
    val anchor = clientTabAnchor()
    val route = clientTabRoute(tab, anchor, settingsRoute)
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentTab = ClientHomeTabNavigator.tabForDestination(currentRoute)
    if (tab == currentTab) {
        val alreadyAtRoot = currentRoute == route
        val poppedToRoot = !alreadyAtRoot && popBackStack(route, inclusive = false)
        if (alreadyAtRoot || poppedToRoot) return
    }
    navigate(route) {
        popUpTo(anchor) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun clientTabRoute(tab: ClientHomeTab, anchor: String, settingsRoute: String): String = when (tab) {
    ClientHomeTab.INICIO -> anchor
    ClientHomeTab.BUSCAR -> HomeDestination.categories
    ClientHomeTab.PEDIDOS -> OrdersDestination.ordersList
    ClientHomeTab.FAVORITOS -> SharedHomeDestination.favorites
    ClientHomeTab.AJUSTES -> settingsRoute
}

/** Destinos raíz de la bottom nav: entre ellos la navegación es un cambio de tab, no un push. */
fun isClientTabRoute(route: String?, settingsRoute: String): Boolean = when (route) {
    null -> false
    settingsRoute -> true
    else -> route in clientTabRootRoutes
}

private val clientTabRootRoutes = setOf(
    HomeDestination.inicio,
    HomeDestination.categories,
    ProviderHomeDestination.inicio,
    OrdersDestination.ordersList,
    SharedHomeDestination.favorites,
)

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
