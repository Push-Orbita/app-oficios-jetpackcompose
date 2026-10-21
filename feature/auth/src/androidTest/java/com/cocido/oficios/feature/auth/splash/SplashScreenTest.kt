package com.cocido.oficios.feature.auth.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun splash_loadingShowsBrand() {
        composeTestRule.setContent {
            OficiosTheme {
                SplashScreen(uiState = SplashUiState.Loading, onRetry = {})
            }
        }
        composeTestRule.onAllNodesWithContentDescription("Oficios Formosa").onFirst().assertIsDisplayed()
    }

    @Test
    fun splash_errorShowsRetry() {
        composeTestRule.setContent {
            OficiosTheme {
                SplashScreen(
                    uiState = SplashUiState.Error("No pudimos conectarnos"),
                    onRetry = {},
                )
            }
        }
        composeTestRule.onNodeWithText("No pudimos iniciar").assertIsDisplayed()
        composeTestRule.onNodeWithText("No pudimos conectarnos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }
}
