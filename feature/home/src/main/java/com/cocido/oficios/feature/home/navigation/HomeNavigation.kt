package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.cocido.oficios.feature.home.CategoriesRoute
import com.cocido.oficios.feature.home.HomeRoute
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.rubros.RubrosRoute
import com.cocido.oficios.feature.home.search.SearchResultsRoute
import com.cocido.oficios.feature.home.worker.WorkerProfileRoute

private const val SettingsRoute = "settings"

fun NavGraphBuilder.homeScreen(
    navController: NavHostController,
    onNavigateToSettings: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    navigation(
        route = HomeDestination.graph,
        startDestination = HomeDestination.inicio,
    ) {
        composable(HomeDestination.inicio) {
            HomeRoute(
                onNavigateToSearch = { navController.openClientTab(ClientHomeTab.BUSCAR, SettingsRoute) },
                onNavigateToRubros = { categoryId, categoryName ->
                    navController.navigate(HomeDestination.rubros(categoryId, categoryName))
                },
                onNavigateToSearchResults = {
                        rubroId,
                        rubroLabel,
                        categoryId,
                        categoryLabel,
                        subRubroId,
                        subRubroLabel,
                        query,
                    ->
                    navController.navigate(
                        HomeDestination.searchResults(
                            rubroId = rubroId,
                            rubroLabel = rubroLabel,
                            categoryId = categoryId,
                            categoryLabel = categoryLabel,
                            subRubroId = subRubroId,
                            subRubroLabel = subRubroLabel,
                            query = query,
                        ),
                    )
                },
                onNavigateToWorker = { workerId ->
                    navController.navigate(HomeDestination.workerProfile(workerId))
                },
                onNavigateToHire = { workerId ->
                    navController.navigate(OrdersDestination.createOrder(workerId))
                },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToHelp = { navController.navigateToHelp() },
                onNavigateToFavorites = { navController.navigateToFavorites() },
                onNavigateToNotifications = { navController.navigateToNotifications() },
                onNavigateToMyOrders = { navController.openClientTab(ClientHomeTab.PEDIDOS, SettingsRoute) },
                onShowMessage = onShowMessage,
            )
        }
        composable(HomeDestination.categories) {
            CategoriesRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInicio = { navController.openClientTab(ClientHomeTab.INICIO, SettingsRoute) },
                onNavigateToSearch = { navController.openClientTab(ClientHomeTab.BUSCAR, SettingsRoute) },
                onNavigateToRubros = { categoryId, categoryName ->
                    navController.navigate(HomeDestination.rubros(categoryId, categoryName))
                },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToHelp = { navController.navigateToHelp() },
                onNavigateToFavorites = { navController.navigateToFavorites() },
                onNavigateToNotifications = { navController.navigateToNotifications() },
                onNavigateToOrders = { navController.openClientTab(ClientHomeTab.PEDIDOS, SettingsRoute) },
                onShowMessage = onShowMessage,
            )
        }
        composable(
            route = HomeDestination.rubros,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            RubrosRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInicio = { navController.openClientTab(ClientHomeTab.INICIO, SettingsRoute) },
                onNavigateToSearch = { navController.openClientTab(ClientHomeTab.BUSCAR, SettingsRoute) },
                onNavigateToSearchResults = { rubroId, rubroLabel, categoryId, categoryLabel, query ->
                    navController.navigate(
                        HomeDestination.searchResults(
                            rubroId = rubroId.takeIf { it >= 0 },
                            rubroLabel = rubroLabel.takeIf { it.isNotBlank() },
                            categoryId = categoryId.takeIf { it >= 0 },
                            categoryLabel = categoryLabel.takeIf { it.isNotBlank() },
                            query = query,
                        ),
                    )
                },
                onShowMessage = onShowMessage,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFavorites = { navController.navigateToFavorites() },
                onNavigateToOrders = { navController.openClientTab(ClientHomeTab.PEDIDOS, SettingsRoute) },
                onNavigateToNotifications = { navController.navigateToNotifications() },
            )
        }
        composable(
            route = HomeDestination.searchResults,
            arguments = listOf(
                navArgument("rubroId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("subRubroId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("barrioId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("query") { type = NavType.StringType; defaultValue = "" },
                navArgument("rubroLabel") { type = NavType.StringType; defaultValue = "" },
                navArgument("subRubroLabel") { type = NavType.StringType; defaultValue = "" },
                navArgument("barrioLabel") { type = NavType.StringType; defaultValue = "" },
                navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("categoryLabel") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            SearchResultsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInicio = { navController.openClientTab(ClientHomeTab.INICIO, SettingsRoute) },
                onNavigateToSearch = { navController.openClientTab(ClientHomeTab.BUSCAR, SettingsRoute) },
                onNavigateToWorker = { workerId ->
                    navController.navigate(HomeDestination.workerProfile(workerId))
                },
                onNavigateToHire = { workerId ->
                    navController.navigate(OrdersDestination.createOrder(workerId))
                },
                onNavigateToNotifications = { navController.navigateToNotifications() },
                onShowMessage = onShowMessage,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFavorites = { navController.navigateToFavorites() },
                onNavigateToOrders = { navController.openClientTab(ClientHomeTab.PEDIDOS, SettingsRoute) },
            )
        }
        composable(
            route = HomeDestination.workerProfile,
            arguments = listOf(
                navArgument("workerId") { type = NavType.LongType },
            ),
        ) {
            WorkerProfileRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInicio = { navController.openClientTab(ClientHomeTab.INICIO, SettingsRoute) },
                onNavigateToSearch = { navController.openClientTab(ClientHomeTab.BUSCAR, SettingsRoute) },
                onNavigateToHire = { workerId ->
                    navController.navigate(OrdersDestination.createOrder(workerId))
                },
                onNavigateToRate = { bookingId, workerName ->
                    navController.navigate(OrdersDestination.rateWorker(bookingId, workerName))
                },
                onNavigateToReport = { workerId, workerName ->
                    navController.navigate(HomeDestination.workerReport(workerId, workerName))
                },
                onShowMessage = onShowMessage,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFavorites = { navController.navigateToFavorites() },
                onNavigateToOrders = {
                    navController.openClientTab(ClientHomeTab.PEDIDOS, SettingsRoute)
                },
            )
        }
        composable(
            route = HomeDestination.workerReport,
            arguments = listOf(
                navArgument("workerId") { type = NavType.LongType },
                navArgument("workerName") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            com.cocido.oficios.feature.home.worker.report.WorkerReportRoute(
                onNavigateBack = { navController.popBackStack() },
                onShowMessage = onShowMessage,
            )
        }
    }
}
