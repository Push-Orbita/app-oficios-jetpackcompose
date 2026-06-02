package com.cocido.oficios.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Rule
import org.junit.Test

class AppButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun oficiosButton_displaysText() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosButton(
                    text = "Continuar",
                    onClick = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Continuar").assertIsDisplayed()
    }

    @Test
    fun oficiosButton_disabled_isNotEnabled() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosButton(
                    text = "Deshabilitado",
                    onClick = {},
                    enabled = false,
                )
            }
        }
        composeTestRule.onNodeWithText("Deshabilitado").assertIsNotEnabled()
    }

    @Test
    fun oficiosButton_secondary_displaysText() {
        composeTestRule.setContent {
            OficiosTheme {
                OficiosButton(
                    text = "Registrarse",
                    onClick = {},
                    variant = OficiosButtonVariant.Secondary,
                )
            }
        }
        composeTestRule.onNodeWithText("Registrarse").assertIsDisplayed()
    }
}
