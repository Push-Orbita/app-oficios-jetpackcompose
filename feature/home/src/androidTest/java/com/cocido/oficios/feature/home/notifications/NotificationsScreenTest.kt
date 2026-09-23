package com.cocido.oficios.feature.home.notifications

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun notificationsScreen_loadingState() {
        composeTestRule.setContent {
            OficiosTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(isLoading = true),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Notificaciones").assertIsDisplayed()
    }

    @Test
    fun notificationsScreen_emptyState() {
        composeTestRule.setContent {
            OficiosTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(items = emptyList()),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sin notificaciones").assertIsDisplayed()
    }

    @Test
    fun notificationsScreen_listStateShowsTitleAndUnreadLabel() {
        composeTestRule.setContent {
            OficiosTheme {
                NotificationsScreen(
                    uiState = NotificationsUiState(
                        items = listOf(
                            com.cocido.oficios.domain.model.NotificationItem(
                                id = 1L,
                                type = "NUEVA_SOLICITUD",
                                title = "Nuevo pedido",
                                body = "Tenés una solicitud pendiente",
                                createdAtLabel = "2 sep 2026, 12:00",
                                isRead = false,
                                entityType = "SOLICITUD",
                                entityId = 42L,
                                deepLink = "orders/42",
                            ),
                        ),
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Nuevo pedido").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nueva").assertIsDisplayed()
    }
}
