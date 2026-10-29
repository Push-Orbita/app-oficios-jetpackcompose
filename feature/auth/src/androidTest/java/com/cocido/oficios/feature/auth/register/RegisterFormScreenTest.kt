package com.cocido.oficios.feature.auth.register

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterFormScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    @Test
    fun clientRegister_showsSeparateNombresAndApellido() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterFormScreen(
                    mode = RegisterMode.CLIENT,
                    uiState = RegisterFormUiState(),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Registro de usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombres").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Apellido").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre y apellido").assertDoesNotExist()
    }

    @Test
    fun providerRegister_showsSeparateNombresAndApellido() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterFormScreen(
                    mode = RegisterMode.PROVIDER,
                    uiState = RegisterFormUiState(),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Registro de trabajador").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombres").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Apellido").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clientRegister_showsFieldErrorsAfterAttempt() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterFormScreen(
                    mode = RegisterMode.CLIENT,
                    uiState = RegisterFormUiState(
                        hasAttemptedSubmit = true,
                        firstNameError = "Los nombres son obligatorios",
                        lastNameError = "El apellido es obligatorio",
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Los nombres son obligatorios").assertIsDisplayed()
        composeTestRule.onNodeWithText("El apellido es obligatorio").assertIsDisplayed()
    }

    @Test
    fun nextButton_isEnabledWhenFormIsInvalidAndNotLoading() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterFormScreen(
                    mode = RegisterMode.PROVIDER,
                    uiState = RegisterFormUiState(
                        firstName = "testingA",
                        lastName = "testingxd",
                        document = "33999222",
                        email = "aikotoba9000@gmail.com",
                        password = "xd123",
                        confirmPassword = "xd123",
                        acceptedTerms = true,
                        canSubmit = false,
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Siguiente").performScrollTo().assertIsEnabled()
    }

    @Test
    fun providerRegister_showsLivePasswordErrorWithoutSubmitAttempt() {
        composeTestRule.setContent {
            OficiosTheme {
                RegisterFormScreen(
                    mode = RegisterMode.PROVIDER,
                    uiState = RegisterFormUiState(
                        password = "xd123",
                        passwordError = "La contraseña tiene que tener al menos 6 caracteres",
                    ),
                    onEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("La contraseña tiene que tener al menos 6 caracteres")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
