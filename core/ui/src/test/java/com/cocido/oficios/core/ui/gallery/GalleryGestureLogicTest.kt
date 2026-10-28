package com.cocido.oficios.core.ui.gallery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryGestureLogicTest {

    @Test
    fun viewerOpensAtTappedIndexAndPaginatesWithinBounds() {
        assertEquals(0, GalleryGesture.initialPage(0, 8))
        assertEquals(2, GalleryGesture.initialPage(2, 8))
        assertEquals(7, GalleryGesture.initialPage(7, 8))
        assertEquals(7, GalleryGesture.initialPage(99, 8))
        assertEquals(0, GalleryGesture.initialPage(-1, 8))
        assertEquals(0, GalleryGesture.initialPage(0, 0))
    }

    @Test
    fun swipeDownAtRestClosesAfterThreshold() {
        assertTrue(GalleryGesture.shouldDismissDrag(pointerCount = 1, scale = 1f, panX = 0f, panY = 40f))
        assertFalse(GalleryGesture.shouldDismissDrag(pointerCount = 1, scale = 2f, panX = 0f, panY = 40f))
        assertFalse(GalleryGesture.shouldDismissDrag(pointerCount = 2, scale = 1f, panX = 0f, panY = 40f))
        assertFalse(GalleryGesture.shouldCloseFromDismiss(139f))
        assertTrue(GalleryGesture.shouldCloseFromDismiss(140f))
    }
}
