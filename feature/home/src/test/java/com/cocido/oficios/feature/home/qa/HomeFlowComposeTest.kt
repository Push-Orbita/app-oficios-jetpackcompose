package com.cocido.oficios.feature.home.qa

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientHomeContent
import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.feature.home.CategoriesScreen
import com.cocido.oficios.feature.home.CategoriesUiState
import com.cocido.oficios.feature.home.HomeScreen
import com.cocido.oficios.feature.home.HomeUiState
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.favorites.FavoritesScreen
import com.cocido.oficios.feature.home.favorites.FavoritesUiState
import com.cocido.oficios.feature.home.help.HelpScreen
import com.cocido.oficios.feature.home.notifications.NotificationsScreen
import com.cocido.oficios.feature.home.notifications.NotificationsUiState
import com.cocido.oficios.feature.home.orders.OrdersListScreen
import com.cocido.oficios.feature.home.orders.OrdersListUiState
import com.cocido.oficios.feature.home.rubros.RubrosScreen
import com.cocido.oficios.feature.home.rubros.RubrosUiState
import com.cocido.oficios.feature.home.search.FilterSheetUiState
import com.cocido.oficios.feature.home.search.SearchResultsScreen
import com.cocido.oficios.feature.home.search.SearchResultsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeFlowComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeLoadingAndError() {
        composeRule.setContent {
            OficiosTheme {
                HomeScreen(
                    uiState = HomeUiState.Error("Sin conexión"),
                    unreadNotificationCount = 0,
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("No pudimos cargar el inicio").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun homeSuccessShowsHeaderWithoutSearchEntry() {
        composeRule.setContent {
            OficiosTheme {
                HomeScreen(
                    uiState = HomeUiState.Success(
                        ClientHomeContent(
                            userDisplayName = "QA",
                            emailVerified = true,
                            profilePhotoUrl = null,
                            recentSearches = emptyList(),
                            categories = listOf(HomeCategoryItem(1, "Hogar")),
                        ),
                    ),
                    unreadNotificationCount = 0,
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("¿Qué oficio está buscando?").assertDoesNotExist()
        composeRule.onNodeWithText("QA").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Notificaciones").assertIsDisplayed()
        composeRule.onNodeWithText("Hogar").assertIsDisplayed()
    }

    @Test
    fun buscarCategoriesKeepsSearchField() {
        composeRule.setContent {
            OficiosTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState.Success(
                        categories = listOf(HomeCategoryItem(1, "Hogar")),
                    ),
                    selectedTab = ClientHomeTab.BUSCAR,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("¿Qué oficio estás buscando?").assertIsDisplayed()
        composeRule.onNodeWithText("Oficios").assertIsDisplayed()
        composeRule.onNodeWithText("Hogar").assertIsDisplayed()
    }

    @Test
    fun buscarRubrosKeepsSearchField() {
        composeRule.setContent {
            OficiosTheme {
                RubrosScreen(
                    uiState = RubrosUiState.Success(
                        categoryName = "Construcción",
                        rubros = listOf(CatalogOption(11, "Plomería")),
                    ),
                    onEvent = {},
                    selectedTab = ClientHomeTab.BUSCAR,
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("¿Qué oficio estás buscando?").assertIsDisplayed()
        composeRule.onNodeWithText("Construcción").assertIsDisplayed()
        composeRule.onNodeWithText("Plomería").assertIsDisplayed()
    }

    @Test
    fun ordersEmptyActivosAndHistorial() {
        composeRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Success(emptyList(), OrdersListFilter.ACTIVE),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeRule.onNodeWithText("Activos").assertIsDisplayed()
        composeRule.onNodeWithText("No tiene pedidos activos").assertIsDisplayed()
    }

    @Test
    fun searchResultsShowsFilterControl() {
        composeRule.setContent {
            OficiosTheme {
                SearchResultsScreen(
                    uiState = SearchResultsUiState.Empty(
                        activeFilters = WorkerSearchFilters(),
                        filterChips = emptyList(),
                    ),
                    filterSheetState = FilterSheetUiState(),
                    searchQuery = "",
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithContentDescription("Filtros").assertIsDisplayed()
    }

    @Test
    fun favoritesEmpty() {
        composeRule.setContent {
            OficiosTheme {
                FavoritesScreen(uiState = FavoritesUiState(), onEvent = {}, onNavigateBack = {})
            }
        }
        composeRule.onNodeWithText("Todavía no tiene favoritos").assertIsDisplayed()
    }

    @Test
    fun notificationsEmpty() {
        composeRule.setContent {
            OficiosTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(items = emptyList()),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeRule.onNodeWithText("Sin notificaciones").assertIsDisplayed()
    }

    @Test
    fun helpFaq() {
        composeRule.setContent {
            OficiosTheme { HelpScreen(onContactSupport = {}, onNavigateBack = {}) }
        }
        composeRule.onNodeWithText("Ayuda").assertIsDisplayed()
    }
}
