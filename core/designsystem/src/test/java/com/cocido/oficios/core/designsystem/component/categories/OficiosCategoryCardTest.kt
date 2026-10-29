package com.cocido.oficios.core.designsystem.component.categories

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w440dp-h980dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosCategoryCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun vectorGlyphMatchesFigmaSizeAndIsCenteredInIconArea() {
        composeRule.setContent {
            OficiosTheme {
                OficiosCategoryCard(
                    title = "Mascotas",
                    onClick = {},
                    modifier = Modifier
                        .width(172.dp)
                        .testTag(CARD_TAG),
                )
            }
        }

        composeRule.onNodeWithText("Mascotas").assertIsDisplayed()

        val card = bounds(CARD_TAG)
        val glyph = bounds(CATEGORY_CARD_GLYPH_TEST_TAG)
        val label = composeRule
            .onNodeWithText("Mascotas", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()

        assertEquals(OficiosComponentSize.authRoleCardMinHeight, card.height)
        assertEquals(OficiosComponentSize.categoryCardGlyphSize, glyph.width)
        assertEquals(OficiosComponentSize.categoryCardGlyphSize, glyph.height)

        assertCenteredHorizontally(card, glyph)
        assertTrue("glifo debajo del label", glyph.top > label.bottom)
        assertTrue(
            "glifo dentro del padding inferior Figma (16dp)",
            glyph.bottom <= card.bottom - OficiosSpacing.md + 1.dp,
        )
        assertCenteredInRemaining(card, label, glyph)
    }

    @Test
    fun illustrationMatchesFigmaBoxAndStaysCentered() {
        composeRule.setContent {
            OficiosTheme {
                OficiosCategoryCard(
                    title = "Pintura",
                    onClick = {},
                    modifier = Modifier
                        .width(172.dp)
                        .testTag(CARD_TAG),
                )
            }
        }

        val card = bounds(CARD_TAG)
        val glyph = bounds(CATEGORY_CARD_GLYPH_TEST_TAG)

        assertEquals(OficiosComponentSize.categoryCardIllustrationWidth, glyph.width)
        assertEquals(OficiosComponentSize.categoryCardIllustrationHeight, glyph.height)
        assertCenteredHorizontally(card, glyph)
    }

    private fun bounds(tag: String): DpRect =
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun assertCenteredHorizontally(card: DpRect, glyph: DpRect) {
        val leftGap = glyph.left - card.left
        val rightGap = card.right - glyph.right
        assertTrue(
            "glifo no centrado H: left=$leftGap right=$rightGap",
            abs(leftGap.value - rightGap.value) <= 2f,
        )
    }

    private fun assertCenteredInRemaining(card: DpRect, label: DpRect, glyph: DpRect) {
        val innerBottom = card.bottom - OficiosSpacing.md
        val remainingCenter = (label.bottom + innerBottom) / 2f
        val glyphCenter = (glyph.top + glyph.bottom) / 2f
        assertTrue(
            "glifo no centrado V en el área bajo el label: remaining=$remainingCenter glyph=$glyphCenter",
            abs(glyphCenter.value - remainingCenter.value) <= 4f,
        )
    }

    companion object {
        private const val CARD_TAG = "oficios_category_card"
    }
}
