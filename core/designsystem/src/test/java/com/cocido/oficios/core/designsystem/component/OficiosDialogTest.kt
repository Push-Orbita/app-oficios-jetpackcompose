package com.cocido.oficios.core.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlin.math.abs
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
class OficiosDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun confirmationActionsSitInEqualWidthRow() {
        composeRule.setContent {
            OficiosTheme {
                OficiosDialog(
                    title = "Eliminar foto",
                    message = "Esta acción no se puede deshacer.",
                    confirmLabel = "Eliminar",
                    onConfirm = {},
                    onDismiss = {},
                    isDangerous = true,
                )
            }
        }
        composeRule.onNodeWithText("Eliminar foto").assertIsDisplayed()
        val cancel = composeRule.onNodeWithText("Cancelar").fetchSemanticsNode()
        val confirm = composeRule.onNodeWithText("Eliminar").fetchSemanticsNode()
        assertTrue(
            "Cancelar a la izquierda de Eliminar: cancel.right=${cancel.boundsInRoot.right} confirm.left=${confirm.boundsInRoot.left}",
            cancel.boundsInRoot.right <= confirm.boundsInRoot.left + 2f,
        )
        val yDiff = abs(cancel.boundsInRoot.top - confirm.boundsInRoot.top)
        assertTrue("Misma fila, yDiff=${yDiff}px", yDiff <= 4f)
        val widthDiff = abs(cancel.size.width - confirm.size.width)
        assertTrue("Mismo ancho, widthDiff=${widthDiff}px", widthDiff <= 4)
    }

    @Test
    fun longLabelsStackAtHighFontScale() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f),
            ) {
                OficiosTheme {
                    OficiosDialog(
                        title = "Eliminar foto",
                        message = "Esta acción no se puede deshacer.",
                        confirmLabel = "Eliminar permanentemente",
                        dismissLabel = "Cancelar y volver",
                        onConfirm = {},
                        onDismiss = {},
                        isDangerous = true,
                    )
                }
            }
        }
        val cancel = composeRule.onNodeWithText("Cancelar y volver").fetchSemanticsNode()
        val confirm = composeRule.onNodeWithText("Eliminar permanentemente").fetchSemanticsNode()
        assertTrue(
            "Apilados a fontScale alto: confirm.top=${confirm.boundsInRoot.top} cancel.bottom=${cancel.boundsInRoot.bottom}",
            confirm.boundsInRoot.top >= cancel.boundsInRoot.bottom - 2f,
        )
    }

    @Test
    fun titleAndMessageStartAligned() {
        composeRule.setContent {
            OficiosTheme {
                OficiosDialog(
                    title = "Eliminar foto",
                    message = "Esta acción no se puede deshacer.",
                    confirmLabel = "Eliminar",
                    onConfirm = {},
                    onDismiss = {},
                    isDangerous = true,
                )
            }
        }
        val title = composeRule.onNodeWithText("Eliminar foto").fetchSemanticsNode()
        val message = composeRule.onNodeWithText("Esta acción no se puede deshacer.").fetchSemanticsNode()
        val xDiff = abs(title.boundsInRoot.left - message.boundsInRoot.left)
        assertTrue("Título y cuerpo alineados a la izquierda, xDiff=${xDiff}px", xDiff <= 2f)
    }
}
