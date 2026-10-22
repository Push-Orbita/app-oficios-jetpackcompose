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

/**
 * Capturas JVM 440×980 mdpi (frame de Figma) para `tools/figma-diff`.
 * Record opt-in: `./gradlew :feature:home:testDebugUnitTest -Proborazzi.test.record=true`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = SEARCH_SCREENSHOT_FIGMA_QUALIFIERS)
class SearchScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun home() {
        assumeGoldenOrRecording(javaClass, "home")
        composeRule.setContent { SearchScreenshotHome() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun homeEmpty() {
        assumeGoldenOrRecording(javaClass, "homeEmpty")
        composeRule.setContent { SearchScreenshotHome(emptyRecents = true) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun categories() {
        assumeGoldenOrRecording(javaClass, "categories")
        composeRule.setContent { SearchScreenshotCategories() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun rubros() {
        assumeGoldenOrRecording(javaClass, "rubros")
        composeRule.setContent { SearchScreenshotRubros() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun results() {
        assumeGoldenOrRecording(javaClass, "results")
        composeRule.setContent { SearchScreenshotResults() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun ficha() {
        assumeGoldenOrRecording(javaClass, "ficha")
        composeRule.setContent { SearchScreenshotFicha() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun providerHome() {
        assumeGoldenOrRecording(javaClass, "providerHome")
        composeRule.setContent { SearchScreenshotProviderHome() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun providerHomeComplete() {
        assumeGoldenOrRecording(javaClass, "providerHomeComplete")
        composeRule.setContent { SearchScreenshotProviderHomeComplete() }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun homeDark() {
        assumeGoldenOrRecording(javaClass, "homeDark")
        composeRule.setContent { SearchScreenshotHome(darkTheme = true) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun fichaDark() {
        assumeGoldenOrRecording(javaClass, "fichaDark")
        composeRule.setContent { SearchScreenshotFicha(darkTheme = true) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun providerHomeDark() {
        assumeGoldenOrRecording(javaClass, "providerHomeDark")
        composeRule.setContent { SearchScreenshotProviderHome(darkTheme = true) }
        composeRule.onRoot().captureRoboImage()
    }
}
