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
class OficiosTextFieldErrorTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `shows supporting error text in red path`() {
        composeRule.setContent {
            OficiosTheme {
                OficiosTextField(
                    value = "mal@",
                    onValueChange = {},
                    label = "Email",
                    isError = true,
                    supportingText = "Email inválido",
                )
            }
        }
        composeRule.onNodeWithText("Email inválido").assertIsDisplayed()
        composeRule.onNodeWithText("Email").assertIsDisplayed()
    }
}
