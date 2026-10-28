package com.cocido.oficios.feature.home.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.FavoriteWorker
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun favoritesScreen_showsEmptyState() {
        composeTestRule.setContent {
            OficiosTheme {
                FavoritesScreen(
                    uiState = FavoritesUiState(),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Favoritos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todavía no tiene favoritos").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Toque el corazón en el perfil de un trabajador para guardarlo acá y encontrarlo más rápido.")
            .assertIsDisplayed()
    }

    @Test
    fun favoritesScreen_showsWorkerCardActions() {
        composeTestRule.setContent {
            OficiosTheme {
                FavoritesScreen(
                    uiState = FavoritesUiState(
                        items = listOf(
                            FavoriteWorker(
                                favoriteId = 1,
                                workerId = 9,
                                displayName = "Trabajador DePrueba",
                                specialtyLabel = "Plomería",
                                rating = 4.8f,
                                avatarUrl = null,
                                isVerified = true,
                            ),
                        ),
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Trabajador DePrueba").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contactar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ver perfil").assertIsDisplayed()
    }
}
