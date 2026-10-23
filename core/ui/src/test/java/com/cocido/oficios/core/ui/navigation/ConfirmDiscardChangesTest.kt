package com.cocido.oficios.core.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
class ConfirmDiscardChangesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dirtyFormShowsDiscardCopyAndKeepsEditingOnDismiss() {
        var exited = false
        composeRule.setContent {
            OficiosTheme {
                val onBack = rememberConfirmDiscardBackHandler(
                    hasUnsavedChanges = true,
                    onConfirmExit = { exited = true },
                )
                TextButton(onClick = onBack) { Text("Volver") }
            }
        }
        composeRule.onNodeWithText("Volver").performClick()
        composeRule.onNodeWithText("¿Descartar cambios?").assertIsDisplayed()
        composeRule.onNodeWithText("Los datos que ingresó se van a perder.").assertIsDisplayed()
        composeRule.onNodeWithText("Seguir editando").assertIsDisplayed()
        composeRule.onNodeWithText("Descartar").assertIsDisplayed()
        composeRule.onNodeWithText("Seguir editando").performClick()
        composeRule.runOnIdle { assertFalse(exited) }
    }

    @Test
    fun discardConfirmsExit() {
        var exited = false
        composeRule.setContent {
            OficiosTheme {
                val onBack = rememberConfirmDiscardBackHandler(
                    hasUnsavedChanges = true,
                    onConfirmExit = { exited = true },
                )
                TextButton(onClick = onBack) { Text("Volver") }
            }
        }
        composeRule.onNodeWithText("Volver").performClick()
        composeRule.onNodeWithText("Descartar").performClick()
        composeRule.runOnIdle { assertTrue(exited) }
    }
}
