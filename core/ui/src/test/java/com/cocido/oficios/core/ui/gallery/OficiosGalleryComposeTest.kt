package com.cocido.oficios.core.ui.gallery

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
class OficiosGalleryComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyMosaicShowsEmptyCopy() {
        composeRule.setContent {
            OficiosTheme {
                OficiosPhotoMosaic(
                    items = emptyList(),
                    onItemClick = {},
                    emptyText = "Todavía no hay fotos de trabajos",
                )
            }
        }
        composeRule.onNodeWithText("Todavía no hay fotos de trabajos").assertIsDisplayed()
    }

    @Test
    fun mosaicClickOpensViewerAtTappedIndex() {
        var opened = -1
        val items = listOf(
            GalleryMedia(1, null, title = null),
            GalleryMedia(2, null, title = null),
            GalleryMedia(3, "https://example.com/c.pdf", title = "Matrícula"),
        )
        composeRule.setContent {
            OficiosTheme {
                OficiosPhotoMosaic(items = items, onItemClick = { opened = it })
            }
        }
        composeRule.onNodeWithContentDescription("Foto 2 de la galería").performClick()
        assertEquals(1, opened)
        composeRule.onNodeWithText("Matrícula").assertIsDisplayed()
    }

    @Test
    fun viewerStartsOnTappedPageAndShowsPagination() {
        composeRule.setContent {
            OficiosTheme {
                OficiosGalleryViewer(
                    items = listOf(
                        GalleryMedia(1, "https://example.com/a.jpg"),
                        GalleryMedia(2, "https://example.com/b.jpg"),
                        GalleryMedia(3, "https://example.com/c.jpg"),
                    ),
                    initialIndex = 2,
                    onClose = {},
                )
            }
        }
        composeRule.onNodeWithText("3 / 3").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Cerrar galería").assertIsDisplayed()
    }
}
