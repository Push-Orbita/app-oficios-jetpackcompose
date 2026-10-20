package com.cocido.oficios.feature.auth.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.auth.login.LOGIN_INVALID_CREDENTIALS_MESSAGE
import com.cocido.oficios.feature.auth.login.LoginScreen
import com.cocido.oficios.feature.auth.login.LoginUiState
import com.cocido.oficios.feature.auth.register.RegisterFormScreen
import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import com.cocido.oficios.feature.auth.register.RegisterMode
import com.cocido.oficios.feature.auth.register.RegisterRoleSelectionScreen
import com.cocido.oficios.feature.auth.splash.SplashScreen
import com.cocido.oficios.feature.auth.splash.SplashUiState
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas JVM 440×980 mdpi (frame de Figma) para `tools/figma-diff`.
 * Splash se posiciona desde el tope del frame (y≈330). El resto simula
 * status bar 48dp + nav 24dp porque Robolectric no aplica safeDrawing.
 *
 * Record opt-in: `./gradlew :feature:auth:testDebugUnitTest -Proborazzi.test.record=true`.
 * Sin esa property el run verifica goldens si existen; si no hay PNG, el test se omite
 * para no romper `testDebugUnitTest`. No se commitean goldens pesados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w440dp-h980dp-mdpi")
class AuthScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splash() {
        assumeGoldenOrRecording("splash")
        composeRule.setContent {
            OficiosTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OficiosColorBackground),
                ) {
                    SplashScreen(uiState = SplashUiState.Loading, onRetry = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun login() {
        assumeGoldenOrRecording("login")
        composeRule.setContent {
            FigmaAuthFrame {
                LoginScreen(uiState = LoginUiState(canSubmit = true), onEvent = {})
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun loginError() {
        assumeGoldenOrRecording("loginError")
        composeRule.setContent {
            FigmaAuthFrame {
                LoginScreen(
                    uiState = LoginUiState(
                        email = "correoescritoeninputselected@mail.com",
                        password = "12345678",
                        bannerError = LOGIN_INVALID_CREDENTIALS_MESSAGE,
                        canSubmit = true,
                        hasAttemptedSubmit = true,
                    ),
                    onEvent = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun role() {
        assumeGoldenOrRecording("role")
        composeRule.setContent {
            FigmaAuthFrame {
                RegisterRoleSelectionScreen(
                    onSelectClient = {},
                    onSelectProvider = {},
                    onNavigateToLogin = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun register() {
        assumeGoldenOrRecording("register")
        composeRule.setContent {
            FigmaAuthFrame {
                RegisterFormScreen(
                    mode = RegisterMode.CLIENT,
                    uiState = RegisterFormUiState(canSubmit = true, acceptedTerms = true),
                    onEvent = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun assumeGoldenOrRecording(methodName: String) {
        val record = System.getProperty("roborazzi.test.record") == "true"
        val className = AuthScreenshotTest::class.java.name
        val candidates = listOf(
            File("src/test/screenshots/$className.$methodName.png"),
            File("src/test/screenshots/com/cocido/oficios/feature/auth/screenshot/AuthScreenshotTest.$methodName.png"),
            File("src/test/screenshots/com/cocido/oficios/feature/auth/screenshot/AuthScreenshotTest/$methodName.png"),
            File("src/screenshots/$className.$methodName.png"),
        )
        Assume.assumeTrue(
            "Sin golden ni -Proborazzi.test.record=true; se omite para no romper testDebugUnitTest",
            record || candidates.any(File::exists),
        )
    }
}

@Composable
private fun FigmaAuthFrame(content: @Composable () -> Unit) {
    OficiosTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OficiosColorBackground),
        ) {
            Spacer(Modifier.fillMaxWidth().height(48.dp))
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Spacer(Modifier.fillMaxWidth().height(24.dp))
        }
    }
}
