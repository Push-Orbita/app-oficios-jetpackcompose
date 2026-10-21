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
 * Capturas JVM Pixel 9 Pro: 427×952 dp, xxhdpi, status 52 + nav 24.
 * Record opt-in; no se commitean PNGs.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = SEARCH_SCREENSHOT_PIXEL9_QUALIFIERS)
class SearchPixel9ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val status = SearchScreenshotChrome.Pixel9ProStatusBar
    private val nav = SearchScreenshotChrome.Pixel9ProNavBar

    @Test
    fun home() {
        assumeGoldenOrRecording(javaClass, "home")
        composeRule.setContent { SearchScreenshotHome(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun homeEmpty() {
        assumeGoldenOrRecording(javaClass, "homeEmpty")
        composeRule.setContent { SearchScreenshotHome(status, nav, emptyRecents = true) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun categories() {
        assumeGoldenOrRecording(javaClass, "categories")
        composeRule.setContent { SearchScreenshotCategories(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun rubros() {
        assumeGoldenOrRecording(javaClass, "rubros")
        composeRule.setContent { SearchScreenshotRubros(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun results() {
        assumeGoldenOrRecording(javaClass, "results")
        composeRule.setContent { SearchScreenshotResults(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun ficha() {
        assumeGoldenOrRecording(javaClass, "ficha")
        composeRule.setContent { SearchScreenshotFicha(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun providerHome() {
        assumeGoldenOrRecording(javaClass, "providerHome")
        composeRule.setContent { SearchScreenshotProviderHome(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun providerHomeComplete() {
        assumeGoldenOrRecording(javaClass, "providerHomeComplete")
        composeRule.setContent { SearchScreenshotProviderHomeComplete(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun orderDetail() {
        assumeGoldenOrRecording(javaClass, "orderDetail")
        composeRule.setContent { SearchScreenshotOrderDetail(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun createOrder() {
        assumeGoldenOrRecording(javaClass, "createOrder")
        composeRule.setContent { SearchScreenshotCreateOrder(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun agendaConfig() {
        assumeGoldenOrRecording(javaClass, "agendaConfig")
        composeRule.setContent { SearchScreenshotAgendaConfig(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun ordersList() {
        assumeGoldenOrRecording(javaClass, "ordersList")
        composeRule.setContent { SearchScreenshotOrdersList(status, nav) }
        composeRule.onRoot().captureRoboImage()
    }
}
