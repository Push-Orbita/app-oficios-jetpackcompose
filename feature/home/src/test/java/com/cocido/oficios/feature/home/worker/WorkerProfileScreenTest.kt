package com.cocido.oficios.feature.home.worker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w440dp-h980dp-mdpi")
class WorkerProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fichaShowsTitleActionsGalleryAndFigmaSections() {
        composeRule.setContent {
            OficiosTheme {
                WorkerProfileScreen(
                    uiState = previewState(withGallery = true, withReviews = true),
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("Ficha Completa").assertIsDisplayed()
        composeRule.onNodeWithText("Emanuel Molina").assertIsDisplayed()
        composeRule.onNodeWithText("Contactar").assertIsDisplayed()
        composeRule.onNodeWithText("Calificar").assertIsDisplayed()
        composeRule.onNodeWithText("Trabajos realizados").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Foto 1 de la galería").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Foto 2 de la galería").assertIsDisplayed()
        composeRule.onNodeWithText("Certificación").assertExists()
        composeRule.onNodeWithText("Certificación pendiente").assertExists()
        composeRule.onNodeWithText("Calificaciones").assertExists()
        composeRule.onNodeWithText("Excelente profesional").assertExists()
        composeRule.onNodeWithText("Denunciar perfil").assertExists()
        composeRule.onNodeWithContentDescription("Guardar en favoritos").assertIsDisplayed()
    }

    @Test
    fun emptyGalleryShowsPendingCopyInsteadOfGiantSlide() {
        composeRule.setContent {
            OficiosTheme {
                WorkerProfileScreen(
                    uiState = previewState(withGallery = false, withReviews = false),
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("Ficha Completa").assertIsDisplayed()
        composeRule.onNodeWithText("Trabajos realizados").assertIsDisplayed()
        composeRule.onNodeWithText("Todavía no hay fotos de trabajos").assertIsDisplayed()
        composeRule.onNodeWithText("Certificación pendiente").assertIsDisplayed()
        composeRule.onNodeWithText("Sin reseñas aún").assertIsDisplayed()
        composeRule.onNodeWithText("Denunciar perfil").assertIsDisplayed()
    }

    @Test
    fun contactRateAndFavoriteKeepTheSameEvents() {
        val events = mutableListOf<WorkerProfileEvent>()
        composeRule.setContent {
            OficiosTheme {
                WorkerProfileScreen(
                    uiState = previewState(withGallery = false, withReviews = false),
                    onEvent = { events += it },
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("Contactar").performClick()
        composeRule.onNodeWithText("Calificar").performClick()
        composeRule.onNodeWithContentDescription("Guardar en favoritos").performClick()
        assertEquals(
            listOf(
                WorkerProfileEvent.HireClicked,
                WorkerProfileEvent.RateClicked,
                WorkerProfileEvent.FavoriteClicked,
            ),
            events,
        )
    }

    @Test
    fun ownProfileHidesContactRateAndFavorite() {
        composeRule.setContent {
            OficiosTheme {
                WorkerProfileScreen(
                    uiState = previewState(withGallery = false, withReviews = false)
                        .copy(isSelf = true, canFavorite = false, rateBookingId = null),
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText("Contactar").assertDoesNotExist()
        composeRule.onNodeWithText("Solicitar").assertDoesNotExist()
        composeRule.onNodeWithText("Calificar").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Guardar en favoritos").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Sacar de favoritos").assertDoesNotExist()
        composeRule.onNodeWithText("Denunciar perfil").assertExists()
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-mdpi")
    fun fichaShowsLongDisplayName() {
        val longName = "Matías Federico Britez Fernández"
        composeRule.setContent {
            OficiosTheme {
                WorkerProfileScreen(
                    uiState = previewState(withGallery = false, withReviews = false).let { state ->
                        state.copy(profile = state.profile.copy(displayName = longName))
                    },
                    onEvent = {},
                    showFavorites = true,
                )
            }
        }
        composeRule.onNodeWithText(longName).assertIsDisplayed()
    }
}
