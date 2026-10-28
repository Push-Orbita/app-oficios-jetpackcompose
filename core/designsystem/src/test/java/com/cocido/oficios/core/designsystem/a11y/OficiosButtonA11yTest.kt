package com.cocido.oficios.core.designsystem.a11y

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosIconButton
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.component.chips.OficiosFilterChip
import com.cocido.oficios.core.designsystem.component.chips.OficiosSegmentChip
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.cards.OficiosSupportCard
import com.cocido.oficios.core.designsystem.component.navigation.OficiosScreenTitle
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import org.junit.Assert.assertTrue
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

    @Test
    fun smallButtonTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosButton(text = "Enviar", onClick = {}, size = OficiosButtonSize.Small)
            }
        }
        assertTrue("Small height=${touchHeightDp("Enviar")}dp", touchHeightDp("Enviar") >= 48f)
    }

    @Test
    fun formButtonVisualHeightIs48dp() {
        var visualHeightDp = 0f
        composeRule.setContent {
            OficiosTheme {
                val density = LocalDensity.current
                OficiosButton(
                    text = "Continuar",
                    onClick = {},
                    size = OficiosButtonSize.Form,
                    modifier = Modifier.onGloballyPositioned { coords ->
                        visualHeightDp = coords.size.height / density.density
                    },
                )
            }
        }
        composeRule.runOnIdle {
            assertTrue(
                "Form visual height=${visualHeightDp}dp should be 48 dp",
                visualHeightDp in 47f..49f,
            )
        }
        assertTrue("Form touch height=${touchHeightDp("Continuar")}dp", touchHeightDp("Continuar") >= 48f)
    }

    @Test
    fun mediumButtonTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosButton(text = "Guardar", onClick = {}, size = OficiosButtonSize.Medium)
            }
        }
        assertTrue("Medium height=${touchHeightDp("Guardar")}dp", touchHeightDp("Guardar") >= 48f)
    }

    @Test
    fun chipTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosChip(label = "Plomería", onClick = {})
            }
        }
        assertTrue("Chip height=${touchHeightDp("Plomería")}dp", touchHeightDp("Plomería") >= 48f)
    }

    @Test
    fun segmentChipTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosSegmentChip(label = "Activos", selected = true, onClick = {})
            }
        }
        assertTrue(
            "Segment height=${touchHeightDp("Activos")}dp",
            touchHeightDp("Activos") >= 48f,
        )
    }

    @Test
    fun filterChipTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosFilterChip(label = "Hoy", selected = false, onToggle = {})
            }
        }
        assertTrue("Filter height=${touchHeightDp("Hoy")}dp", touchHeightDp("Hoy") >= 48f)
    }

    @Test
    fun filterChipVisualHeightDoesNotInflateToTouchTarget() {
        var visualHeightDp = 0f
        composeRule.setContent {
            OficiosTheme {
                val density = LocalDensity.current
                OficiosFilterChip(
                    label = "Hoy",
                    selected = false,
                    onToggle = {},
                    modifier = Modifier.onGloballyPositioned { coords ->
                        visualHeightDp = coords.size.height / density.density
                    },
                )
            }
        }
        composeRule.runOnIdle {
            assertTrue(
                "Filter visual height=${visualHeightDp}dp should stay in the 36–44 dp slot",
                visualHeightDp in 32f..44f,
            )
            assertTrue(
                "Filter visual height=${visualHeightDp}dp must not inflate to 48",
                visualHeightDp < 48f,
            )
        }
        assertTrue("Filter touch height=${touchHeightDp("Hoy")}dp", touchHeightDp("Hoy") >= 48f)
    }

    @Test
    fun smallButtonVisualHeightDoesNotInflateToTouchTarget() {
        var visualHeightDp = 0f
        composeRule.setContent {
            OficiosTheme {
                val density = LocalDensity.current
                OficiosButton(
                    text = "Enviar",
                    onClick = {},
                    size = OficiosButtonSize.Small,
                    modifier = Modifier.onGloballyPositioned { coords ->
                        visualHeightDp = coords.size.height / density.density
                    },
                )
            }
        }
        composeRule.runOnIdle {
            assertTrue(
                "Small visual height=${visualHeightDp}dp should stay near 36 dp",
                visualHeightDp in 32f..44f,
            )
            assertTrue(
                "Small visual height=${visualHeightDp}dp must not inflate to 48",
                visualHeightDp < 48f,
            )
        }
        assertTrue("Small touch height=${touchHeightDp("Enviar")}dp", touchHeightDp("Enviar") >= 48f)
    }

    @Test
    fun iconButtonTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    onClick = {},
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription("Cerrar").fetchSemanticsNode()
        val heightDp = node.size.height / node.layoutInfo.density.density
        assertTrue("IconButton height=${heightDp}dp", heightDp >= 48f)
    }

    @Test
    fun passwordVisibilityIconTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosPasswordField(
                    value = "secret",
                    onValueChange = {},
                    label = "Contraseña",
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription("Mostrar contraseña")
            .fetchSemanticsNode()
        val heightDp = node.size.height / node.layoutInfo.density.density
        val widthDp = node.size.width / node.layoutInfo.density.density
        assertTrue("Password eye height=${heightDp}dp", heightDp >= 48f)
        assertTrue("Password eye width=${widthDp}dp", widthDp >= 48f)
    }

    @Test
    fun supportCardHelpIconIsInsideCardAndContactStays48() {
        composeRule.setContent {
            OficiosTheme {
                OficiosSupportCard(
                    message = "Para cualquier consulta o duda de asistencia, contáctenos a nuestra línea de atención.",
                    contactLabel = "Contactar",
                    onContactClick = {},
                )
            }
        }
        val icon = composeRule.onNodeWithContentDescription("Ayuda").fetchSemanticsNode()
        val density = icon.layoutInfo.density.density
        val heightDp = icon.size.height / density
        val widthDp = icon.size.width / density
        assertTrue("Help icon height=${heightDp}dp", heightDp in 31f..33f)
        assertTrue("Help icon width=${widthDp}dp", widthDp in 31f..33f)
        composeRule.onNodeWithText("Contactar").assertIsDisplayed()
        composeRule.onNodeWithText("?").assertIsDisplayed()
        val contactH = touchHeightDp("Contactar")
        val contactW = touchWidthDp("Contactar")
        assertTrue("Contactar height=${contactH}dp", contactH >= 48f)
        assertTrue("Contactar width=${contactW}dp", contactW >= 48f)
    }

    @Test
    fun screenTitleBackTouchTargetAtLeast48dp() {
        composeRule.setContent {
            OficiosTheme {
                OficiosScreenTitle(title = "Pedido Confirmado", onNavigateBack = {})
            }
        }
        val node = composeRule.onNodeWithContentDescription("Volver").fetchSemanticsNode()
        val heightDp = node.size.height / node.layoutInfo.density.density
        val widthDp = node.size.width / node.layoutInfo.density.density
        assertTrue("Back height=${heightDp}dp", heightDp >= 48f)
        assertTrue("Back width=${widthDp}dp", widthDp >= 48f)
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
