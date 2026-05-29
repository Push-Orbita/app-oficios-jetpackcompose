package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.home.HomeRoute

fun NavGraphBuilder.homeScreen(
    onNavigateToSettings: () -> Unit,
) {
    composable(route = HomeDestination.route) {
        HomeRoute(onNavigateToSettings = onNavigateToSettings)
    }
}
