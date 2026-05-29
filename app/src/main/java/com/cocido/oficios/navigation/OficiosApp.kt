package com.cocido.oficios.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cocido.oficios.feature.home.navigation.HomeDestination
import com.cocido.oficios.feature.home.navigation.homeScreen
import com.cocido.oficios.feature.settings.navigation.SettingsDestination
import com.cocido.oficios.feature.settings.navigation.settingsScreen

@Composable
fun OficiosApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    AppNavHost(
        navController = navController,
        modifier = modifier,
    )
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = HomeDestination.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onNavigateToSettings = {
                navController.navigate(SettingsDestination.route)
            },
        )
        settingsScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
