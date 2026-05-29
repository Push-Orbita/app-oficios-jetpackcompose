package com.cocido.oficios.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cocido.oficios.core.designsystem.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class AppButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appButton_displaysText() {
        composeTestRule.setContent {
            AppTheme {
                AppButton(
                    text = "Continue",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }
}
