package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h900dp-xxhdpi")
class PasswordRequirementsChecklistTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders title live requirement labels and symbol definition`() {
        composeRule.setContent {
            OficiosTheme {
                PasswordRequirementsChecklist(
                    requirements = listOf(
                        PasswordRequirementItem("Entre 6 y 64 caracteres", satisfied = false),
                        PasswordRequirementItem("Al menos una letra", satisfied = true),
                        PasswordRequirementItem("Al menos un número", satisfied = false),
                        PasswordRequirementItem("Al menos un símbolo (por ej. ! @ # $ - _)", satisfied = false),
                    ),
                )
            }
        }
        composeRule.onNodeWithText("Tu contraseña necesita:").assertIsDisplayed()
        composeRule.onNodeWithText("Entre 6 y 64 caracteres").assertIsDisplayed()
        composeRule.onNodeWithText("Al menos una letra").assertIsDisplayed()
        composeRule.onNodeWithText("Al menos un número").assertIsDisplayed()
        composeRule.onNodeWithText("Al menos un símbolo (por ej. ! @ # $ - _)").assertIsDisplayed()
        composeRule.onNodeWithText("Símbolo: cualquier carácter que no sea letra, número ni espacio.").assertIsDisplayed()
    }
}
