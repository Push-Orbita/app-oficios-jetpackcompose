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
 * Registro y Rol a fontScale 1.3 (Pixel 9 Pro). Verifica que «términos y
 * condiciones» haga wrap y que las cards de rol usen heightIn(min) sin recorte.
 * Record opt-in; no se commitean goldens.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = AUTH_SCREENSHOT_PIXEL9_QUALIFIERS)
class AuthFontScaleScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val status = AuthScreenshotChrome.Pixel9ProStatusBar
    private val nav = AuthScreenshotChrome.Pixel9ProNavBar

    @Test
    fun registerFontScale13() {
        assumeGoldenOrRecording(javaClass, "registerFontScale13")
        composeRule.setContent {
            AuthFontScaleOverride(fontScale = 1.3f) {
                AuthScreenshotRegister(status, nav)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun roleFontScale13() {
        assumeGoldenOrRecording(javaClass, "roleFontScale13")
        composeRule.setContent {
            AuthFontScaleOverride(fontScale = 1.3f) {
                AuthScreenshotRole(status, nav)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
