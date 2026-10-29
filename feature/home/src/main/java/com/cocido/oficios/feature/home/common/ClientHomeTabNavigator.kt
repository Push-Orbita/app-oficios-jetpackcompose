package com.cocido.oficios.feature.home.common

import com.cocido.oficios.feature.home.components.ClientHomeTab

sealed interface ClientHomeTabNavigation {
    data object Stay : ClientHomeTabNavigation
    data object Search : ClientHomeTabNavigation
    data object Orders : ClientHomeTabNavigation
    data object Settings : ClientHomeTabNavigation
    data object Favorites : ClientHomeTabNavigation
}

object ClientHomeTabNavigator {

    fun navigationFor(tab: ClientHomeTab): ClientHomeTabNavigation = when (tab) {
        ClientHomeTab.INICIO -> ClientHomeTabNavigation.Stay
        ClientHomeTab.BUSCAR -> ClientHomeTabNavigation.Search
        ClientHomeTab.PEDIDOS -> ClientHomeTabNavigation.Orders
        ClientHomeTab.FAVORITOS -> ClientHomeTabNavigation.Favorites
        ClientHomeTab.AJUSTES -> ClientHomeTabNavigation.Settings
    }

    /**
     * Pestaña visible según la ruta actual. Subpantallas (detalle, datos personales)
     * no son pestañas: caen en Inicio salvo el destino raíz de cada tab.
     */
    fun tabForDestination(route: String?): ClientHomeTab = when {
        route == null -> ClientHomeTab.INICIO
        route == "orders/mine" -> ClientHomeTab.PEDIDOS
        route == "favorites" -> ClientHomeTab.FAVORITOS
        route == "settings" -> ClientHomeTab.AJUSTES
        isBuscarRoute(route) -> ClientHomeTab.BUSCAR
        else -> ClientHomeTab.INICIO
    }

    fun handleTabSelected(
        tab: ClientHomeTab,
        onStay: () -> Unit = {},
        onNavigateToSearch: () -> Unit = {},
        onNavigateToOrders: () -> Unit = {},
        onNavigateToSettings: () -> Unit,
        onNavigateToFavorites: () -> Unit,
    ) {
        when (navigationFor(tab)) {
            ClientHomeTabNavigation.Stay -> onStay()
            ClientHomeTabNavigation.Search -> onNavigateToSearch()
            ClientHomeTabNavigation.Orders -> onNavigateToOrders()
            ClientHomeTabNavigation.Settings -> onNavigateToSettings()
            ClientHomeTabNavigation.Favorites -> onNavigateToFavorites()
        }
    }
}

internal fun isBuscarRoute(route: String): Boolean =
    route == "home/categories" ||
        route.startsWith("home/rubros") ||
        route.startsWith("home/search")
