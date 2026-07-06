package com.cocido.oficios.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.cocido.oficios.feature.home.CategoriesRoute
import com.cocido.oficios.feature.home.HomeRoute
import com.cocido.oficios.feature.home.rubros.RubrosRoute
import com.cocido.oficios.feature.home.search.SearchResultsRoute
import com.cocido.oficios.feature.home.search.WorkerStubRoute

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
                onNavigateToSearch = {
                    navController.navigate(HomeDestination.categories) {
                        launchSingleTop = true
                    }
                },
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
                    navController.navigate(HomeDestination.workerStub(workerId))
                },
                onNavigateToSettings = onNavigateToSettings,
                onShowMessage = onShowMessage,
            )
        }
        composable(HomeDestination.categories) {
            CategoriesRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRubros = { categoryId, categoryName ->
                    navController.navigate(HomeDestination.rubros(categoryId, categoryName))
                },
                onNavigateToSearchResults = { query ->
                    navController.navigate(HomeDestination.searchResults(query = query))
                },
                onNavigateToSettings = onNavigateToSettings,
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
                onNavigateToSearchResults = { rubroId, rubroLabel, categoryId, categoryLabel, query ->
                    navController.navigate(
                        HomeDestination.searchResults(
                            rubroId = rubroId.takeIf { it >= 0 },
                            rubroLabel = rubroLabel.takeIf { it.isNotBlank() },
                            categoryId = categoryId,
                            categoryLabel = categoryLabel,
                            query = query,
                        ),
                    )
                },
                onShowMessage = onShowMessage,
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
                onNavigateToWorker = { workerId ->
                    navController.navigate(HomeDestination.workerStub(workerId))
                },
                onShowMessage = onShowMessage,
            )
        }
        composable(
            route = HomeDestination.workerStub,
            arguments = listOf(
                navArgument("workerId") { type = NavType.LongType },
            ),
        ) {
            WorkerStubRoute(onNavigateBack = { navController.popBackStack() })
        }
    }
}
