package com.cocido.oficios.feature.auth.qa

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import com.cocido.oficios.feature.auth.splash.SplashBrandTestTag
import kotlin.math.abs
import org.junit.Assert.assertTrue
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.forgot.ForgotPasswordScreen
import com.cocido.oficios.feature.auth.forgot.ForgotPasswordUiState
import com.cocido.oficios.feature.auth.login.LoginScreen
import com.cocido.oficios.feature.auth.login.LoginUiState
import com.cocido.oficios.feature.auth.register.RegisterFormScreen
import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import com.cocido.oficios.feature.auth.register.RegisterMode
import com.cocido.oficios.feature.auth.splash.SplashScreen
import com.cocido.oficios.feature.auth.splash.SplashUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthFlowComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splashLoadingShowsBrand() {
        composeRule.setContent {
            OficiosTheme { SplashScreen(uiState = SplashUiState.Loading, onRetry = {}) }
        }
        composeRule.onAllNodesWithContentDescription("Oficios Formosa").onFirst().assertIsDisplayed()
    }

    @Test
    fun splashBrandLockupIsCentered() {
        composeRule.setContent {
            OficiosTheme { SplashScreen(uiState = SplashUiState.Loading, onRetry = {}) }
        }
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val brand = composeRule.onNodeWithTag(SplashBrandTestTag).getUnclippedBoundsInRoot()
        val rootCenterX = (root.left + root.right) / 2
        val rootCenterY = (root.top + root.bottom) / 2
        val brandCenterX = (brand.left + brand.right) / 2
        val brandCenterY = (brand.top + brand.bottom) / 2
        assertTrue(
            "lockup X ${brandCenterX.value} vs screen ${rootCenterX.value}",
            abs(brandCenterX.value - rootCenterX.value) <= 8f,
        )
        assertTrue(
            "lockup Y ${brandCenterY.value} vs screen ${rootCenterY.value}",
            abs(brandCenterY.value - rootCenterY.value) <= 16f,
        )
    }

    @Test
    fun splashErrorShowsRetry() {
        composeRule.setContent {
            OficiosTheme {
                SplashScreen(uiState = SplashUiState.Error("Sin conexión"), onRetry = {})
            }
        }
        composeRule.onNodeWithText("No pudimos iniciar").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun loginUnauthorizedBanner() {
        composeRule.setContent {
            OficiosTheme {
                LoginScreen(
                    uiState = LoginUiState(bannerError = "Los datos de sesión no coinciden"),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Los datos de sesión no coinciden").assertIsDisplayed()
        composeRule.onNodeWithText("Correo").assertIsDisplayed()
    }

    @Test
    fun registerClientShowsNombresAndApellido() {
        composeRule.setContent {
            OficiosTheme {
                RegisterFormScreen(
                    mode = RegisterMode.CLIENT,
                    uiState = RegisterFormUiState(),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Nombres").assertIsDisplayed()
        composeRule.onNodeWithText("Apellido").assertIsDisplayed()
    }

    @Test
    fun registerNextStaysEnabledWhenFormIsInvalid() {
        composeRule.setContent {
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
                        passwordError = "La contraseña tiene que tener al menos 6 caracteres",
                        canSubmit = false,
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Siguiente").performScrollTo().assertIsEnabled()
        composeRule.onNodeWithText("La contraseña tiene que tener al menos 6 caracteres")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun forgotPasswordShowsEmailCta() {
        composeRule.setContent {
            OficiosTheme {
                ForgotPasswordScreen(
                    uiState = ForgotPasswordUiState(),
                    onEvent = {},
                    onBackToLogin = {},
                )
            }
        }
        composeRule.onNodeWithText("Recuperar contraseña").assertIsDisplayed()
        composeRule.onNodeWithText("Enviar instrucciones").performScrollTo().assertIsDisplayed()
    }
}
