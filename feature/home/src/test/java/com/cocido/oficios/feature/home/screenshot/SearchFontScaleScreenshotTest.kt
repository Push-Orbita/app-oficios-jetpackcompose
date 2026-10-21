package com.cocido.oficios.feature.home.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** fontScale 1.3: el texto no debe recortarse (heightIn min, no alturas fijas). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = SEARCH_SCREENSHOT_FIGMA_QUALIFIERS)
class SearchFontScaleScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeFontScale() {
        assumeGoldenOrRecording(javaClass, "homeFontScale")
        composeRule.setContent {
            SearchFontScaleOverride(fontScale = 1.3f) {
                SearchScreenshotHome()
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun resultsFontScale() {
        assumeGoldenOrRecording(javaClass, "resultsFontScale")
        composeRule.setContent {
            SearchFontScaleOverride(fontScale = 1.3f) {
                SearchScreenshotResults()
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
