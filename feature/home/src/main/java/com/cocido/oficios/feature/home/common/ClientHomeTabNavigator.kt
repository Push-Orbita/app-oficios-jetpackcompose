package com.cocido.oficios.feature.home.common

import com.cocido.oficios.feature.home.components.ClientHomeTab

sealed interface ClientHomeTabNavigation {
    data object Stay : ClientHomeTabNavigation
    data object Settings : ClientHomeTabNavigation
    data object PersonalData : ClientHomeTabNavigation
    data object Help : ClientHomeTabNavigation
    data object Favorites : ClientHomeTabNavigation
}

object ClientHomeTabNavigator {

    fun navigationFor(tab: ClientHomeTab): ClientHomeTabNavigation = when (tab) {
        ClientHomeTab.INICIO -> ClientHomeTabNavigation.Stay
        ClientHomeTab.PERFIL -> ClientHomeTabNavigation.PersonalData
        ClientHomeTab.FAVORITOS -> ClientHomeTabNavigation.Favorites
        ClientHomeTab.AYUDA -> ClientHomeTabNavigation.Help
        ClientHomeTab.AJUSTES -> ClientHomeTabNavigation.Settings
    }

    fun handleTabSelected(
        tab: ClientHomeTab,
        onStay: () -> Unit = {},
        onNavigateToSettings: () -> Unit,
        onNavigateToPersonalData: () -> Unit,
        onNavigateToHelp: () -> Unit,
        onNavigateToFavorites: () -> Unit,
    ) {
        when (navigationFor(tab)) {
            ClientHomeTabNavigation.Stay -> onStay()
            ClientHomeTabNavigation.Settings -> onNavigateToSettings()
            ClientHomeTabNavigation.PersonalData -> onNavigateToPersonalData()
            ClientHomeTabNavigation.Help -> onNavigateToHelp()
            ClientHomeTabNavigation.Favorites -> onNavigateToFavorites()
        }
    }
}
