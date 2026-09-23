package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cocido.oficios.feature.home.favorites.FavoritesRoute
import com.cocido.oficios.feature.home.help.HelpRoute
import com.cocido.oficios.feature.home.notifications.NotificationsRoute

fun NavGraphBuilder.sharedHomeScreens(
    navController: NavHostController,
    onShowMessage: (String) -> Unit,
    onNavigateToWorker: (Long) -> Unit,
    onNavigateToHire: (Long) -> Unit,
) {
    composable(SharedHomeDestination.help) {
        HelpRoute(onNavigateBack = { navController.popBackStack() })
    }
    composable(SharedHomeDestination.favorites) {
        FavoritesRoute(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToWorker = onNavigateToWorker,
            onNavigateToHire = onNavigateToHire,
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

fun NavHostController.navigateToPersonalData() {
    navigate(SharedHomeDestination.personalData) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToHelp() {
    navigate(SharedHomeDestination.help) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToFavorites() {
    navigate(SharedHomeDestination.favorites) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToNotifications() {
    navigate(SharedHomeDestination.notifications) {
        launchSingleTop = true
    }
}
