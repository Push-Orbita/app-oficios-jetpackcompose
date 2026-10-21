package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.cocido.oficios.feature.home.providerhome.ProviderHomeRoute

fun NavGraphBuilder.providerHomeScreen(
    navController: NavHostController,
    onNavigateToSettings: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToCommercialLocal: () -> Unit,
    onNavigateToCertificate: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    navigation(
        route = ProviderHomeDestination.graph,
        startDestination = ProviderHomeDestination.inicio,
    ) {
        composable(ProviderHomeDestination.inicio) {
            ProviderHomeRoute(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToHelp = { navController.navigateToHelp() },
                onNavigateToFavorites = { navController.navigateToFavorites() },
                onNavigateToNotifications = { navController.navigateToNotifications() },
                onNavigateToGallery = onNavigateToGallery,
                onNavigateToCommercialLocal = onNavigateToCommercialLocal,
                onNavigateToCertificate = onNavigateToCertificate,
                onNavigateToOrdersList = {
                    navController.openClientTab(
                        com.cocido.oficios.feature.home.components.ClientHomeTab.PEDIDOS,
                        "settings",
                    )
                },
                onNavigateToOrderDetail = { orderId -> navController.navigate(OrdersDestination.orderDetail(orderId)) },
                onNavigateToAgenda = { navController.navigate(AgendaDestination.list) },
                onNavigateToAgendaConfig = { navController.navigate(AgendaDestination.config) },
                onShowMessage = onShowMessage,
            )
        }
    }
}
