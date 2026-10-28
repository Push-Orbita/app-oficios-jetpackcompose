package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosSupportCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
    fun at360dpNothingOverflowsAndButtonIsNotSqueezed() {
        assertSupportCardLayout(containerWidthDp = 360, screenPadding = true)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w412dp-h800dp-mdpi")
    fun at412dpNothingOverflowsAndButtonIsNotSqueezed() {
        assertSupportCardLayout(containerWidthDp = 412, screenPadding = true)
    }

    @Test
    @Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
    fun at360dpFullWidthStillKeepsIconInsideBorder() {
        assertSupportCardLayout(containerWidthDp = 360, screenPadding = false)
    }

    private fun assertSupportCardLayout(containerWidthDp: Int, screenPadding: Boolean) {
        var cardBounds = Rect.Zero
        var iconBounds = Rect.Zero
        var contactBounds = Rect.Zero
        var standaloneButtonWidth = 0

        composeRule.setContent {
            OficiosTheme {
                Box(Modifier.width(containerWidthDp.dp)) {
                    OficiosButton(
                        text = "Contactar",
                        onClick = {},
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Small,
                        figmaAuthStroke = true,
                        modifier = Modifier
                            .testTag("standalone_contact")
                            .onGloballyPositioned { standaloneButtonWidth = it.size.width },
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .then(
                                if (screenPadding) Modifier.padding(horizontal = 32.dp) else Modifier,
                            ),
                    ) {
                        OficiosSupportCard(
                            message = MESSAGE,
                            contactLabel = "Contactar",
                            onContactClick = {},
                            modifier = Modifier
                                .onGloballyPositioned { cardBounds = it.boundsInRoot() },
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("?").assertIsDisplayed()
        composeRule.onNodeWithTag(OFICIOS_SUPPORT_CONTACT_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(MESSAGE).assertIsDisplayed()

        iconBounds = composeRule.onNodeWithTag(OFICIOS_SUPPORT_HELP_ICON_TEST_TAG)
            .fetchSemanticsNode().boundsInRoot
        contactBounds = composeRule.onNodeWithTag(OFICIOS_SUPPORT_CONTACT_TEST_TAG)
            .fetchSemanticsNode().boundsInRoot
        val cardNode = composeRule.onNodeWithTag(OFICIOS_SUPPORT_CARD_TEST_TAG).fetchSemanticsNode()
        cardBounds = cardNode.boundsInRoot

        assertContained("icon", iconBounds, cardBounds)
        assertContained("contact", contactBounds, cardBounds)
        assertFalse("icon overlaps contact", iconBounds.overlaps(contactBounds))

        val layout = textLayout()
        assertFalse("texto recortado (visual overflow)", layout.hasVisualOverflow)

        val inCardWidth = composeRule.onNodeWithTag(OFICIOS_SUPPORT_CONTACT_TEST_TAG)
            .fetchSemanticsNode().size.width
        val delta = 2
        assertTrue(
            "botón apretado: standalone=$standaloneButtonWidth inCard=$inCardWidth",
            kotlin.math.abs(inCardWidth - standaloneButtonWidth) <= delta ||
                inCardWidth >= standaloneButtonWidth - delta,
        )
    }

    private fun textLayout(): TextLayoutResult {
        val node = composeRule.onNodeWithText(MESSAGE).fetchSemanticsNode()
        val action = node.config.getOrNull(SemanticsActions.GetTextLayoutResult)
        val out = mutableListOf<TextLayoutResult>()
        check(action != null && action.action!!.invoke(out)) { "sin TextLayoutResult" }
        return out.first()
    }

    private fun assertContained(name: String, inner: Rect, outer: Rect) {
        assertTrue(
            "$name fuera de la card inner=$inner outer=$outer",
            inner.left >= outer.left - 1f &&
                inner.top >= outer.top - 1f &&
                inner.right <= outer.right + 1f &&
                inner.bottom <= outer.bottom + 1f,
        )
    }

    companion object {
        private const val MESSAGE =
            "Para cualquier consulta o duda de asistencia, contáctenos a nuestra línea de atención."
    }
}
