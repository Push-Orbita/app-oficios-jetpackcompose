package com.cocido.oficios.feature.auth.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
@Config(sdk = [34], qualifiers = AUTH_SCREENSHOT_FIGMA_QUALIFIERS)
class AuthScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun splash() {
        assumeGoldenOrRecording(javaClass, "splash")
        composeRule.setContent { AuthScreenshotSplash() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun login() {
        assumeGoldenOrRecording(javaClass, "login")
        composeRule.setContent { AuthScreenshotLogin() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun loginError() {
        assumeGoldenOrRecording(javaClass, "loginError")
        composeRule.setContent { AuthScreenshotLoginError() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun role() {
        assumeGoldenOrRecording(javaClass, "role")
        composeRule.setContent { AuthScreenshotRole() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun register() {
        assumeGoldenOrRecording(javaClass, "register")
        composeRule.setContent { AuthScreenshotRegister() }
        composeRule.onRoot().captureRoboImage()
    }
}
