package com.cocido.oficios.feature.home.qa

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.feature.home.CategoriesScreen
import com.cocido.oficios.feature.home.CategoriesUiState
import com.cocido.oficios.feature.home.HomeScreen
import com.cocido.oficios.feature.home.HomeUiState
import com.cocido.oficios.feature.home.agenda.AgendaConfigScreen
import com.cocido.oficios.feature.home.agenda.AgendaConfigUiState
import com.cocido.oficios.feature.home.agenda.AgendaListScreen
import com.cocido.oficios.feature.home.agenda.AgendaListUiState
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.favorites.FavoritesScreen
import com.cocido.oficios.feature.home.favorites.FavoritesUiState
import com.cocido.oficios.feature.home.notifications.NotificationsScreen
import com.cocido.oficios.feature.home.notifications.NotificationsUiState
import com.cocido.oficios.feature.home.orders.CreateOrderScreen
import com.cocido.oficios.feature.home.orders.CreateOrderUiState
import com.cocido.oficios.feature.home.orders.MisUbicacionesScreen
import com.cocido.oficios.feature.home.orders.MisUbicacionesUiState
import com.cocido.oficios.feature.home.orders.OrderDetailScreen
import com.cocido.oficios.feature.home.orders.OrderDetailUiState
import com.cocido.oficios.feature.home.orders.OrdersListScreen
import com.cocido.oficios.feature.home.orders.OrdersListUiState
import com.cocido.oficios.feature.home.providerhome.ProviderHomeScreen
import com.cocido.oficios.feature.home.providerhome.ProviderHomeUiState
import com.cocido.oficios.feature.home.rubros.RubrosScreen
import com.cocido.oficios.feature.home.rubros.RubrosUiState
import com.cocido.oficios.feature.home.search.FilterSheetUiState
import com.cocido.oficios.feature.home.search.SearchResultsScreen
import com.cocido.oficios.feature.home.search.SearchResultsUiState
import com.cocido.oficios.feature.home.worker.WorkerProfileScreen
import com.cocido.oficios.feature.home.worker.WorkerProfileUiState
import com.cocido.oficios.feature.home.worker.report.WorkerReportScreen
import com.cocido.oficios.feature.home.worker.report.WorkerReportUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ErrorCopyComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeErrorCopy() {
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
        assertErrorCopy("No pudimos cargar el inicio")
    }

    @Test
    fun searchErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                SearchResultsScreen(
                    uiState = SearchResultsUiState.Error("Sin conexión"),
                    filterSheetState = FilterSheetUiState(),
                    searchQuery = "",
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        assertErrorCopy("No pudimos completar la búsqueda", homeTitleMustBeAbsent = true)
    }

    @Test
    fun providerHomeErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                ProviderHomeScreen(
                    uiState = ProviderHomeUiState.Error("Sin conexión"),
                    unreadNotificationCount = 0,
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su inicio", homeTitleMustBeAbsent = true)
    }

    @Test
    fun categoriesErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState.Error("Sin conexión"),
                    selectedTab = ClientHomeTab.INICIO,
                    onEvent = {},
                    onTabSelected = {},
                    showFavorites = true,
                )
            }
        }
        assertErrorCopy("No pudimos cargar los oficios", homeTitleMustBeAbsent = true)
    }

    @Test
    fun rubrosErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                RubrosScreen(
                    uiState = RubrosUiState.Error("Sin conexión"),
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        assertErrorCopy("No pudimos cargar los rubros", homeTitleMustBeAbsent = true)
    }

    @Test
    fun workerProfileErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                WorkerProfileScreen(
                    uiState = WorkerProfileUiState.Error("Sin conexión"),
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        assertErrorCopy("No pudimos cargar la ficha del trabajador", homeTitleMustBeAbsent = true)
    }

    @Test
    fun ordersListErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus pedidos", homeTitleMustBeAbsent = true)
    }

    @Test
    fun orderDetailErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                OrderDetailScreen(
                    uiState = OrderDetailUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar el pedido", homeTitleMustBeAbsent = true)
    }

    @Test
    fun locationsErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                MisUbicacionesScreen(
                    uiState = MisUbicacionesUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                    showFavorites = true,
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus ubicaciones", homeTitleMustBeAbsent = true)
    }

    @Test
    fun createOrderErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                CreateOrderScreen(
                    uiState = CreateOrderUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                    showFavorites = true,
                )
            }
        }
        assertErrorCopy("No pudimos cargar los datos", homeTitleMustBeAbsent = true)
    }

    @Test
    fun favoritesErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                FavoritesScreen(
                    uiState = FavoritesUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus favoritos", homeTitleMustBeAbsent = true)
    }

    @Test
    fun notificationsErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(isLoading = false, errorMessage = "Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar sus notificaciones", homeTitleMustBeAbsent = true)
    }

    @Test
    fun agendaListErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                AgendaListScreen(
                    uiState = AgendaListUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su agenda", homeTitleMustBeAbsent = true)
    }

    @Test
    fun agendaConfigErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                AgendaConfigScreen(
                    uiState = AgendaConfigUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar su configuración", homeTitleMustBeAbsent = true)
    }

    @Test
    fun workerReportErrorCopy() {
        composeRule.setContent {
            OficiosTheme {
                WorkerReportScreen(
                    uiState = WorkerReportUiState(
                        isLoadingReasons = false,
                        reasonsErrorMessage = "Sin conexión",
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        assertErrorCopy("No pudimos cargar los motivos", homeTitleMustBeAbsent = true)
    }

    private fun assertErrorCopy(title: String, homeTitleMustBeAbsent: Boolean = false) {
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithText("Reintentar").fetchSemanticsNodes().isNotEmpty())
        if (homeTitleMustBeAbsent) {
            assertEquals(
                0,
                composeRule.onAllNodesWithText("No pudimos cargar el inicio").fetchSemanticsNodes().size,
            )
        }
    }
}
