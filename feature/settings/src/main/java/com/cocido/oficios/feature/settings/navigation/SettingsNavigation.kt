package com.cocido.oficios.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.settings.SettingsRoute

fun NavGraphBuilder.settingsScreen(
    onNavigateBack: () -> Unit,
) {
    composable(route = SettingsDestination.route) {
        SettingsRoute(onNavigateBack = onNavigateBack)
    }
}
