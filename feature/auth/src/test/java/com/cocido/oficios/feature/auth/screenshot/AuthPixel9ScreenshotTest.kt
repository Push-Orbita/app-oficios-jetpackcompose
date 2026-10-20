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
 * Capturas JVM Pixel 9 Pro: 427×952 dp, xxhdpi (density 3), status 52 + nav 24.
 * Mide en dp (px/3) relativos al bottom del status bar. El +4 vs Figma 48 no
 * se compensa con spacers.
 *
 * Record opt-in: `./gradlew :feature:auth:testDebugUnitTest -Proborazzi.test.record=true`.
 * Sin golden ni record, se omite. No se commitean PNGs.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = AUTH_SCREENSHOT_PIXEL9_QUALIFIERS)
class AuthPixel9ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val status = AuthScreenshotChrome.Pixel9ProStatusBar
    private val nav = AuthScreenshotChrome.Pixel9ProNavBar

    @Test
    fun login() {
        assumeGoldenOrRecording(javaClass, "login")
        composeRule.setContent { AuthScreenshotLogin(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun loginError() {
        assumeGoldenOrRecording(javaClass, "loginError")
        composeRule.setContent { AuthScreenshotLoginError(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun role() {
        assumeGoldenOrRecording(javaClass, "role")
        composeRule.setContent { AuthScreenshotRole(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun register() {
        assumeGoldenOrRecording(javaClass, "register")
        composeRule.setContent { AuthScreenshotRegister(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }
}
