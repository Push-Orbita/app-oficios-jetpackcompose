package com.cocido.oficios.feature.auth.forgot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun forgotPassword_showsEmailAndSendCta() {
        composeTestRule.setContent {
            OficiosTheme {
                ForgotPasswordScreen(
                    uiState = ForgotPasswordUiState(),
                    onEvent = {},
                    onBackToLogin = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Recuperar contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Le enviaremos un código a su correo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enviar instrucciones").assertIsDisplayed()
    }

    @Test
    fun forgotPassword_successState() {
        composeTestRule.setContent {
            OficiosTheme {
                ForgotPasswordScreen(
                    uiState = ForgotPasswordUiState(isSuccess = true),
                    onEvent = {},
                    onBackToLogin = {},
                )
            }
        }
        composeTestRule.onNodeWithText(
            "Si el correo existe, le enviamos instrucciones para recuperar su contraseña.",
        ).assertIsDisplayed()
    }

    @Test
    fun forgotPassword_errorState() {
        composeTestRule.setContent {
            OficiosTheme {
                ForgotPasswordScreen(
                    uiState = ForgotPasswordUiState(generalError = "No pudimos conectarnos"),
                    onEvent = {},
                    onBackToLogin = {},
                )
            }
        }
        composeTestRule.onNodeWithText("No pudimos conectarnos").assertIsDisplayed()
    }
}
