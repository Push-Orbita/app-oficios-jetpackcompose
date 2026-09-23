package com.cocido.oficios.feature.home.help

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelpScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun helpScreen_showsFaqAndContactButton() {
        composeTestRule.setContent {
            OficiosTheme {
                HelpScreen(
                    onContactSupport = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Ayuda").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿Cómo contrato a un trabajador?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contactar").assertIsDisplayed()
    }
}
