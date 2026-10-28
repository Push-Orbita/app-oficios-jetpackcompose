package com.cocido.oficios.feature.home.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryGestureTest {

    @Test
    fun `single finger at rest is left for the pager`() {
        assertFalse(GalleryGesture.shouldConsumeDrag(pointerCount = 1, scale = 1f))
    }

    @Test
    fun `pinch or pan while zoomed is consumed`() {
        assertTrue(GalleryGesture.shouldConsumeDrag(pointerCount = 2, scale = 1f))
        assertTrue(GalleryGesture.shouldConsumeDrag(pointerCount = 1, scale = 2.5f))
    }

    @Test
    fun `pan is clamped to the scaled image edges and cleared at rest`() {
        val clamped = GalleryGesture.clampPan(
            x = 500f,
            y = -400f,
            scale = 2f,
            viewportWidth = 200f,
            viewportHeight = 100f,
        )
        assertEquals(100f, clamped.x)
        assertEquals(-50f, clamped.y)

        val rest = GalleryGesture.clampPan(10f, 10f, scale = 1f, viewportWidth = 200f, viewportHeight = 100f)
        assertEquals(0f, rest.x)
        assertEquals(0f, rest.y)
    }

    @Test
    fun viewerOpensAtTappedIndex() {
        assertEquals(2, GalleryGesture.initialPage(2, 8))
        assertEquals(0, GalleryGesture.initialPage(-4, 8))
        assertEquals(7, GalleryGesture.initialPage(40, 8))
    }

    @Test
    fun swipeDownDismissesOnlyPastThreshold() {
        assertTrue(GalleryGesture.shouldDismissDrag(1, 1f, 4f, 30f))
        assertFalse(GalleryGesture.shouldCloseFromDismiss(100f))
        assertTrue(GalleryGesture.shouldCloseFromDismiss(140f))
    }
}
