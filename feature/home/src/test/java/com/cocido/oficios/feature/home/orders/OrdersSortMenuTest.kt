package com.cocido.oficios.feature.home.orders

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.OrdersListSort
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrdersSortMenuTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun closedMenuShowsCurrentSortAndHidesTheOtherOption() {
        composeRule.setContent {
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
        composeRule.onNodeWithText("Activos").assertIsDisplayed()
        composeRule.onNodeWithText("Historial").assertIsDisplayed()
        composeRule.onNodeWithText("Más antiguos").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Orden de sus pedidos: Más recientes")
            .assertIsDisplayed()
    }

    @Test
    fun menuOffersBothSortsAndCheckStaysOnTheSelectedOne() {
        val events = mutableListOf<OrdersListEvent>()
        composeRule.setContent {
            OficiosTheme {
                OrdersListScreen(
                    uiState = OrdersListUiState.Success(
                        orders = emptyList(),
                        filter = OrdersListFilter.ACTIVE,
                    ),
                    onEvent = { events += it },
                    onNavigateBack = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription("Orden de sus pedidos: Más recientes")
            .performClick()
        composeRule.onNodeWithText("Más recientes").assertIsDisplayed()
        composeRule.onNodeWithText("Más antiguos").assertIsDisplayed()
        composeRule.onNodeWithText("Más antiguos").performClick()
        assertEquals(listOf(OrdersListEvent.SortSelected(OrdersListSort.OLDEST_FIRST)), events)
    }
}
