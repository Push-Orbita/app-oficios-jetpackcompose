package com.cocido.oficios.feature.home.orders

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrdersListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun orders_loadingExposesContentDescription() {
        composeTestRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Loading,
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Mis pedidos").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cargando pedidos…").assertIsDisplayed()
    }

    @Test
    fun orders_errorShowsRetry() {
        composeTestRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Error("Sin conexión"),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeTestRule.onNodeWithText("No pudimos cargar sus pedidos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun orders_emptyActivos() {
        composeTestRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Success(
                        orders = emptyList(),
                        filter = OrdersListFilter.ACTIVE,
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Activos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Historial").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Orden de sus pedidos: Más recientes")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("No tiene pedidos activos").assertIsDisplayed()
    }

    @Test
    fun orders_emptyHistorial() {
        composeTestRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Success(
                        orders = emptyList(),
                        filter = OrdersListFilter.HISTORY,
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Todavía no hay historial").assertIsDisplayed()
    }
}
