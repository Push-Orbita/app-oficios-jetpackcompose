package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeBenefitTileTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
    fun at360dpFontScale1TextIsCompleteAndUnbroken() {
        assertBenefitsLayout(containerWidthDp = 360, fontScale = 1.0f)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
    fun at360dpFontScale13TextIsCompleteAndUnbroken() {
        assertBenefitsLayout(containerWidthDp = 360, fontScale = 1.3f)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w412dp-h800dp-mdpi")
    fun at412dpFontScale1TextIsCompleteAndUnbroken() {
        assertBenefitsLayout(containerWidthDp = 412, fontScale = 1.0f)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w412dp-h800dp-mdpi")
    fun at412dpFontScale13TextIsCompleteAndUnbroken() {
        assertBenefitsLayout(containerWidthDp = 412, fontScale = 1.3f)
    }

    private fun assertBenefitsLayout(containerWidthDp: Int, fontScale: Float) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
            ) {
                OficiosTheme {
                    Box(
                        Modifier
                            .width(containerWidthDp.dp)
                            .padding(horizontal = OficiosSpacing.xl)
                            .testTag("benefits_host"),
                    ) {
                        HomeBenefitsRow(tiles = TILES)
                    }
                }
            }
        }

        COPIES.forEach { copy ->
            composeRule.onNodeWithText(copy).assertExists()
            val layout = textLayout(copy)
            assertFalse(
                "texto recortado ($copy @${containerWidthDp}dp ${fontScale}x)",
                layout.hasVisualOverflow,
            )
            assertFalse(
                "palabra partida ($copy @${containerWidthDp}dp ${fontScale}x): ${layout.lineBreaks(copy)}",
                layout.breaksMidWord(copy),
            )
        }

        val heights = (0..2).map { index ->
            composeRule.onNodeWithTag("$HOME_BENEFIT_TILE_TEST_TAG_PREFIX$index")
                .fetchSemanticsNode().size.height
        }
        assertEquals("cards de distinta altura $heights", 1, heights.distinct().size)
    }

    private fun textLayout(text: String): TextLayoutResult {
        val node = composeRule.onNodeWithText(text).fetchSemanticsNode()
        val action = node.config.getOrNull(SemanticsActions.GetTextLayoutResult)
        val out = mutableListOf<TextLayoutResult>()
        check(action != null && action.action!!.invoke(out)) { "sin TextLayoutResult para $text" }
        return out.first()
    }

    private fun TextLayoutResult.breaksMidWord(text: String): Boolean {
        for (line in 0 until lineCount) {
            val start = getLineStart(line)
            if (start in 1 until text.length) {
                val previous = text[start - 1]
                val current = text[start]
                if (previous.isLetterOrDigit() && current.isLetterOrDigit()) return true
            }
        }
        return false
    }

    private fun TextLayoutResult.lineBreaks(text: String): String =
        (0 until lineCount).joinToString(" | ") { line ->
            val start = getLineStart(line)
            val end = getLineEnd(line, visibleEnd = true).coerceAtMost(text.length)
            text.substring(start, end)
        }

    companion object {
        private val COPIES = listOf(
            "Todos sus servicios en un mismo espacio",
            "Mayor alcance a potenciales clientes",
            "Organícese de manera más eficiente",
        )
        private val TILES = listOf(
            emphasizedBody(COPIES[0], "Todos"),
            emphasizedBody(COPIES[1], "alcance"),
            emphasizedBody(COPIES[2], "eficiente"),
        )
    }
}
