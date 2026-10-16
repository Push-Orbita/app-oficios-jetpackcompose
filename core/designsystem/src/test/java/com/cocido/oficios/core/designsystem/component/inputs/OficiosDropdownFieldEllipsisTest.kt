package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h3000dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosDropdownFieldEllipsisTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val longValue = "Casa QA · Av. 25 de Mayo 450 · Villa Hermosa"

    @Test
    fun valorLargoConFoco_unaLineaConElipsis_fuente100() = assertDropdownEllipsisWithFocus(fontScale = 1.0f)

    @Test
    fun valorLargoConFoco_unaLineaConElipsis_fuente130() = assertDropdownEllipsisWithFocus(fontScale = 1.3f)

    @Test
    fun valorLargoConFoco_unaLineaConElipsis_fuente200() = assertDropdownEllipsisWithFocus(fontScale = 2.0f)

    private fun assertDropdownEllipsisWithFocus(fontScale: Float) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                OficiosTheme {
                    Column(modifier = Modifier.width(360.dp)) {
                        Box(Modifier.testTag("dropdown")) {
                            OficiosDropdownField(
                                value = longValue,
                                options = listOf(longValue, "Otra opción"),
                                onOptionSelected = {},
                                label = "Dirección",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Box(Modifier.testTag("referencia")) {
                            OficiosTextField(
                                value = longValue,
                                onValueChange = {},
                                label = "Dirección",
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        composeTestRule.onNode(
            hasTestTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG) and hasAnyAncestor(hasTestTag("dropdown")),
            useUnmergedTree = true,
        ).performClick()
        composeTestRule.waitForIdle()

        val dropdownBorder = composeTestRule.onNode(
            hasTestTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG) and hasAnyAncestor(hasTestTag("dropdown")),
            useUnmergedTree = true,
        ).fetchSemanticsNode()
        val referenceBorder = composeTestRule.onNode(
            hasTestTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG) and hasAnyAncestor(hasTestTag("referencia")),
            useUnmergedTree = true,
        ).fetchSemanticsNode()

        assertEquals(
            "altura del dropdown @${fontScale}x difiere del campo readonly de una línea",
            referenceBorder.size.height,
            dropdownBorder.size.height,
        )

        val ellipsisLayout = composeTestRule.onNode(
            hasText(longValue) and !isFocused() and hasAnyAncestor(hasTestTag("dropdown")),
            useUnmergedTree = true,
        ).fetchSemanticsNode().textLayout()
            ?: error("sin layout del valor con elipsis @${fontScale}x")

        assertEquals(
            "el valor visible debe ocupar una sola línea @${fontScale}x",
            1,
            ellipsisLayout.lineCount,
        )

        val focusedLayout = composeTestRule.onNode(
            isFocused() and hasAnyAncestor(hasTestTag("dropdown")),
            useUnmergedTree = true,
        ).fetchSemanticsNode().textLayout()
            ?: error("sin layout del anchor con foco @${fontScale}x")
        assertTrue(
            "anchor con foco no debe desbordar en alto @${fontScale}x",
            !focusedLayout.didOverflowHeight,
        )
        assertEquals(
            "el anchor con foco debe medir una sola línea @${fontScale}x",
            1,
            focusedLayout.lineCount,
        )
    }

    private fun SemanticsNode.textLayout(): TextLayoutResult? {
        val results = mutableListOf<TextLayoutResult>()
        val action = config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return null
        return if (action(results)) results.firstOrNull() else null
    }
}
