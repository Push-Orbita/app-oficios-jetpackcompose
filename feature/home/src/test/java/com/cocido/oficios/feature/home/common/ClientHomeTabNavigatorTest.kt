package com.cocido.oficios.feature.home.common

import com.cocido.oficios.feature.home.components.ClientHomeTab
import org.junit.Assert.assertEquals
import org.junit.Test

class ClientHomeTabNavigatorTest {

    @Test
    fun `inicio tab stays on home`() {
        assertEquals(ClientHomeTabNavigation.Stay, ClientHomeTabNavigator.navigationFor(ClientHomeTab.INICIO))
    }

    @Test
    fun `pedidos tab stays so the bottom bar remains visible`() {
        assertEquals(ClientHomeTabNavigation.Stay, ClientHomeTabNavigator.navigationFor(ClientHomeTab.PEDIDOS))
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
    fun `handleTabSelected does not leave home for pedidos`() {
        var stayed = false
        var settingsCalled = false
        ClientHomeTabNavigator.handleTabSelected(
            tab = ClientHomeTab.PEDIDOS,
            onStay = { stayed = true },
            onNavigateToSettings = { settingsCalled = true },
            onNavigateToHelp = {},
            onNavigateToFavorites = {},
        )
        assertEquals(true, stayed)
        assertEquals(false, settingsCalled)
    }
}
