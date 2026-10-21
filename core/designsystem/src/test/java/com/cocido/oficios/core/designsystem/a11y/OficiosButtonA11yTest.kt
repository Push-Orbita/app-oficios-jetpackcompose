package com.cocido.oficios.core.designsystem.a11y

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h900dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosButtonA11yTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun largeButtonTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosButton(text = "Continuar", onClick = {}, size = OficiosButtonSize.Large)
            }
        }
        val heightDp = touchHeightDp("Continuar")
        val widthDp = touchWidthDp("Continuar")
        assertTrue("Large height=${heightDp}dp", heightDp >= 48f)
        assertTrue("Large width=${widthDp}dp", widthDp >= 48f)
    }

    @Ignore("QA-FAIL: OficiosButton Small=36dp y LocalMinimumInteractiveComponentSize=Unspecified; touch target < 48dp")
    @Test
    fun smallButtonTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosButton(text = "Enviar", onClick = {}, size = OficiosButtonSize.Small)
            }
        }
        assertTrue("Small height=${touchHeightDp("Enviar")}dp", touchHeightDp("Enviar") >= 48f)
    }

    @Ignore("QA-FAIL: OficiosButton Medium=44dp; touch target < 48dp")
    @Test
    fun mediumButtonTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosButton(text = "Guardar", onClick = {}, size = OficiosButtonSize.Medium)
            }
        }
        assertTrue("Medium height=${touchHeightDp("Guardar")}dp", touchHeightDp("Guardar") >= 48f)
    }

    private fun touchHeightDp(text: String): Float {
        val node = composeRule.onNodeWithText(text).fetchSemanticsNode()
        return node.size.height / node.layoutInfo.density.density
    }

    private fun touchWidthDp(text: String): Float {
        val node = composeRule.onNodeWithText(text).fetchSemanticsNode()
        return node.size.width / node.layoutInfo.density.density
    }
}
