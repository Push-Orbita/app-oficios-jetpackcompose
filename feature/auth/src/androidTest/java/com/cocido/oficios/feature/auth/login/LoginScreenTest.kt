package com.cocido.oficios.feature.auth.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import com.cocido.oficios.feature.auth.TestHostActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun loginScreen_displaysFieldsAndRememberMe() {
        composeTestRule.setContent {
            OficiosTheme {
                LoginScreen(
                    uiState = LoginUiState(),
                    onEvent = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Correo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recordarme").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿Olvidó su contraseña?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_forgotPasswordInvokesEvent() {
        var forgotPasswordClicked = false
        composeTestRule.setContent {
            OficiosTheme {
                LoginScreen(
                    uiState = LoginUiState(),
                    onEvent = { event ->
                        if (event is LoginEvent.ForgotPasswordClicked) {
                            forgotPasswordClicked = true
                        }
                    },
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("¿Olvidó su contraseña?").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000) { forgotPasswordClicked }
        assertTrue(forgotPasswordClicked)
    }

    @Test
    fun loginScreen_loadingDisablesForgotPassword() {
        composeTestRule.setContent {
            OficiosTheme {
                LoginScreen(
                    uiState = LoginUiState(isLoading = true),
                    onEvent = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("¿Olvidó su contraseña?").assertIsNotEnabled()
    }
}
