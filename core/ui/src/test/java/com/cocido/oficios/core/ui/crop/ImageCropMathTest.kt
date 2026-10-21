package com.cocido.oficios.core.ui.crop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageCropMathTest {

    @Test
    fun `rotate clockwise wraps at 360`() {
        assertEquals(90, rotateClockwise(0))
        assertEquals(180, rotateClockwise(90))
        assertEquals(270, rotateClockwise(180))
        assertEquals(0, rotateClockwise(270))
    }

    @Test
    fun `rotated size swaps on 90 and 270`() {
        assertEquals(400 to 200, rotatedSize(400, 200, 0))
        assertEquals(200 to 400, rotatedSize(400, 200, 90))
        assertEquals(400 to 200, rotatedSize(400, 200, 180))
        assertEquals(200 to 400, rotatedSize(400, 200, 270))
    }

    @Test
    fun `square crop window is centered 1 to 1`() {
        val window = cropWindow(400f, 800f, ImageCropAspect.Square, insetFraction = 0.1f)
        assertEquals(window.width, window.height)
        val centerX = (window.left + window.right) / 2
        assertEquals(200, centerX)
    }

    @Test
    fun `gallery 4 3 window keeps ratio`() {
        val window = cropWindow(400f, 400f, ImageCropAspect.Gallery43, insetFraction = 0f)
        val ratio = window.width.toFloat() / window.height
        assertEquals(4f / 3f, ratio, 0.02f)
    }

    @Test
    fun `identity transform on covering image crops the full rotated bitmap`() {
        val transform = ImageCropTransform(aspect = ImageCropAspect.Free)
        val crop = bitmapCropRect(
            imageWidth = 400,
            imageHeight = 300,
            transform = transform,
            viewportWidth = 400f,
            viewportHeight = 300f,
        )
        assertEquals(0, crop.left)
        assertEquals(0, crop.top)
        assertEquals(400, crop.right)
        assertEquals(300, crop.bottom)
    }

    @Test
    fun `rotation 90 maps crop into swapped dimensions`() {
        val transform = ImageCropTransform(rotationDegrees = 90, aspect = ImageCropAspect.Free)
        val crop = bitmapCropRect(
            imageWidth = 400,
            imageHeight = 200,
            transform = transform,
            viewportWidth = 200f,
            viewportHeight = 400f,
        )
        assertEquals(200, crop.width)
        assertEquals(400, crop.height)
        assertEquals(0, crop.left)
        assertEquals(0, crop.top)
    }

    @Test
    fun `pinch scale 2 on square still stays inside the bitmap`() {
        val transform = ImageCropTransform(scale = 2f, aspect = ImageCropAspect.Square)
        val crop = bitmapCropRect(
            imageWidth = 400,
            imageHeight = 400,
            transform = transform,
            viewportWidth = 400f,
            viewportHeight = 400f,
        )
        assertTrue(crop.left >= 0)
        assertTrue(crop.top >= 0)
        assertTrue(crop.right <= 400)
        assertTrue(crop.bottom <= 400)
        assertTrue(crop.width < 400)
        assertTrue(crop.height < 400)
        assertEquals(crop.width, crop.height)
    }

    @Test
    fun `pan right reveals the left of the image`() {
        val base = ImageCropTransform(scale = 2f, aspect = ImageCropAspect.Square)
        val centered = bitmapCropRect(400, 400, base, 400f, 400f)
        val panned = bitmapCropRect(400, 400, base.copy(panX = 80f), 400f, 400f)
        assertTrue(panned.left < centered.left)
        assertTrue(panned.left >= 0)
        assertTrue(panned.right <= 400)
        assertEquals(panned.width, panned.height)
    }

    @Test
    fun `profile mode is circular square and gallery allows free`() {
        assertEquals(ImageCropAspect.Square, ImageCropMode.Profile.defaultAspect())
        assertTrue(ImageCropMode.Profile.circularMask())
        assertEquals(ImageCropAspect.Gallery43, ImageCropMode.Gallery.defaultAspect())
        assertTrue(ImageCropMode.Gallery.allowsFreeAspect())
        assertEquals(ImageCropAspect.Free, ImageCropMode.Certificate.defaultAspect())
        assertEquals(ImageCropAspect.Free, ImageCropMode.Document.defaultAspect())
        assertFalse(ImageCropMode.Certificate.circularMask())
        assertFalse(ImageCropMode.Certificate.allowsFreeAspect())
    }
}
