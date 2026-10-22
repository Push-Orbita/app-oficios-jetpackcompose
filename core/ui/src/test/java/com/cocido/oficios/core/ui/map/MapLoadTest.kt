package com.cocido.oficios.core.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapLoadTest {

    @Test
    fun timeoutDoesNotFireUntilVisible() {
        assertFalse(shouldStartMapTimeout(apiKeyBlank = false, visibleOnScreen = false))
        assertTrue(shouldStartMapTimeout(apiKeyBlank = false, visibleOnScreen = true))
        assertFalse(shouldStartMapTimeout(apiKeyBlank = true, visibleOnScreen = true))
    }

    @Test
    fun timeoutMarksUnavailableOnlyIfStillUnloaded() {
        val pending = MapLoadState()
        assertTrue(pending.onTimeout().showFallback)
        val loaded = MapLoadState(loaded = true)
        assertFalse(loaded.onTimeout().showFallback)
        assertTrue(loaded.onTimeout().showGoogleMap)
    }

    @Test
    fun retryRemountsAndClearsFallback() {
        val failed = MapLoadState(generation = 0, unavailable = true)
        val retried = failed.retry(apiKeyBlank = false)
        assertEquals(1, retried.generation)
        assertTrue(retried.showSpinner)
        assertTrue(retried.showGoogleMap)
        assertFalse(retried.showFallback)
    }

    @Test
    fun offScreenBoundsDoNotCountAsVisible() {
        assertFalse(
            isMapOnScreen(
                boundsLeft = 0f,
                boundsTop = 1200f,
                boundsRight = 360f,
                boundsBottom = 1460f,
                viewportWidth = 360f,
                viewportHeight = 800f,
            ),
        )
        assertTrue(
            isMapOnScreen(
                boundsLeft = 0f,
                boundsTop = 500f,
                boundsRight = 360f,
                boundsBottom = 760f,
                viewportWidth = 360f,
                viewportHeight = 800f,
            ),
        )
    }

    @Test
    fun timeoutIsFifteenSeconds() {
        assertEquals(15_000L, MAP_LOAD_TIMEOUT_MILLIS)
    }
}
