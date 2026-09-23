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
    fun `perfil tab navigates to personal data`() {
        assertEquals(ClientHomeTabNavigation.PersonalData, ClientHomeTabNavigator.navigationFor(ClientHomeTab.PERFIL))
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
    fun `handleTabSelected invokes personal data callback for perfil`() {
        var personalDataCalled = false
        ClientHomeTabNavigator.handleTabSelected(
            tab = ClientHomeTab.PERFIL,
            onNavigateToSettings = {},
            onNavigateToPersonalData = { personalDataCalled = true },
            onNavigateToHelp = {},
            onNavigateToFavorites = {},
        )
        assertEquals(true, personalDataCalled)
    }
}
