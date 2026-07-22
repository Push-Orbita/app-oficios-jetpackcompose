package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.cocido.oficios.feature.home.providerhome.ProviderHomeRoute

fun NavGraphBuilder.providerHomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    navigation(
        route = ProviderHomeDestination.graph,
        startDestination = ProviderHomeDestination.inicio,
    ) {
        composable(ProviderHomeDestination.inicio) {
            ProviderHomeRoute(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToCompleteProfile = onNavigateToCompleteProfile,
                onShowMessage = onShowMessage,
            )
        }
    }
}
