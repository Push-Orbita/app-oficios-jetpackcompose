package com.cocido.oficios.feature.home.common

import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.navigation.clientTabRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class ClientHomeTabNavigatorTest {

    @Test
    fun `inicio tab stays on home`() {
        assertEquals(ClientHomeTabNavigation.Stay, ClientHomeTabNavigator.navigationFor(ClientHomeTab.INICIO))
    }

    @Test
    fun `pedidos is its own destination so system back returns to inicio`() {
        assertEquals(ClientHomeTabNavigation.Orders, ClientHomeTabNavigator.navigationFor(ClientHomeTab.PEDIDOS))
        assertEquals("orders/mine", clientTabRoute(ClientHomeTab.PEDIDOS, "home/inicio", "settings"))
        assertEquals(ClientHomeTab.INICIO, ClientHomeTabNavigator.tabForDestination("home/inicio"))
        assertEquals(ClientHomeTab.PEDIDOS, ClientHomeTabNavigator.tabForDestination("orders/mine"))
    }

    @Test
    fun `favoritos tab navigates to favorites`() {
        assertEquals(ClientHomeTabNavigation.Favorites, ClientHomeTabNavigator.navigationFor(ClientHomeTab.FAVORITOS))
    }

    @Test
    fun `ayuda tab navigates to help`() {
        assertEquals(ClientHomeTabNavigation.Help, ClientHomeTabNavigator.navigationFor(ClientHomeTab.AYUDA))
    }

    @Test
    fun `ajustes tab navigates to settings`() {
        assertEquals(ClientHomeTabNavigation.Settings, ClientHomeTabNavigator.navigationFor(ClientHomeTab.AJUSTES))
    }

    @Test
    fun `handleTabSelected leaves home for pedidos`() {
        var stayed = false
        var ordersCalled = false
        ClientHomeTabNavigator.handleTabSelected(
            tab = ClientHomeTab.PEDIDOS,
            onStay = { stayed = true },
            onNavigateToOrders = { ordersCalled = true },
            onNavigateToSettings = {},
            onNavigateToHelp = {},
            onNavigateToFavorites = {},
        )
        assertEquals(false, stayed)
        assertEquals(true, ordersCalled)
    }

    @Test
    fun `reselecting inicio always lands on home`() {
        assertEquals("home/inicio", clientTabRoute(ClientHomeTab.INICIO, "home/inicio", "settings"))
        assertEquals(
            "providerHome/inicio",
            clientTabRoute(ClientHomeTab.INICIO, "providerHome/inicio", "settings"),
        )
        assertEquals(ClientHomeTab.INICIO, ClientHomeTabNavigator.tabForDestination("home/inicio"))
        assertEquals(ClientHomeTab.INICIO, ClientHomeTabNavigator.tabForDestination("providerHome/inicio"))
        assertEquals(ClientHomeTab.INICIO, ClientHomeTabNavigator.tabForDestination("home/categories"))
        assertEquals(ClientHomeTab.INICIO, ClientHomeTabNavigator.tabForDestination("orders/12"))
        var stayed = false
        var ordersCalled = false
        ClientHomeTabNavigator.handleTabSelected(
            tab = ClientHomeTab.INICIO,
            onStay = { stayed = true },
            onNavigateToOrders = { ordersCalled = true },
            onNavigateToSettings = {},
            onNavigateToHelp = {},
            onNavigateToFavorites = {},
        )
        assertEquals(true, stayed)
        assertEquals(false, ordersCalled)
    }

    @Test
    fun `client tabs resolve to top level routes`() {
        assertEquals("home/inicio", clientTabRoute(ClientHomeTab.INICIO, "home/inicio", "settings"))
        assertEquals("orders/mine", clientTabRoute(ClientHomeTab.PEDIDOS, "home/inicio", "settings"))
        assertEquals("favorites", clientTabRoute(ClientHomeTab.FAVORITOS, "home/inicio", "settings"))
        assertEquals("help", clientTabRoute(ClientHomeTab.AYUDA, "home/inicio", "settings"))
        assertEquals("settings", clientTabRoute(ClientHomeTab.AJUSTES, "home/inicio", "settings"))
        assertEquals(
            "providerHome/inicio",
            clientTabRoute(ClientHomeTab.INICIO, "providerHome/inicio", "settings"),
        )
    }
}
