package com.cocido.oficios.core.ui.crop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun `initial free crop is the full 16 by 9 image`() {
        val crop = initialCropRect(1920, 1080, ImageCropAspect.Free)
        assertEquals(0, crop.left)
        assertEquals(0, crop.top)
        assertEquals(1920, crop.right)
        assertEquals(1080, crop.bottom)
        val applied = bitmapCropRect(
            imageWidth = 1920,
            imageHeight = 1080,
            transform = ImageCropTransform(aspect = ImageCropAspect.Free),
        )
        assertEquals(crop, applied)
    }

    @Test
    fun `initial free crop is the full 9 by 16 image`() {
        val crop = initialCropRect(1080, 1920, ImageCropAspect.Free)
        assertEquals(0, crop.left)
        assertEquals(0, crop.top)
        assertEquals(1080, crop.right)
        assertEquals(1920, crop.bottom)
    }

    @Test
    fun `square start is the largest centered square inside a 16 by 9 image`() {
        val crop = initialCropRect(1600, 900, ImageCropAspect.Square)
        assertEquals(crop.width, crop.height)
        assertEquals(900, crop.width)
        assertEquals(350, crop.left)
        assertEquals(0, crop.top)
        assertEquals(1250, crop.right)
        assertEquals(900, crop.bottom)
    }

    @Test
    fun `gallery 4 3 start is the largest centered 4 3 inside the image`() {
        val crop = initialCropRect(1600, 900, ImageCropAspect.Gallery43)
        val ratio = crop.width.toFloat() / crop.height
        assertEquals(4f / 3f, ratio, 0.02f)
        assertEquals(0, crop.top)
        assertEquals(900, crop.bottom)
        assertTrue(crop.left >= 0)
        assertTrue(crop.right <= 1600)
    }

    @Test
    fun `resize right handle clamps to the image edge`() {
        val start = initialCropRect(400, 300, ImageCropAspect.Free)
        val shrunk = resizeCrop(
            crop = start,
            handle = CropHandle.Right,
            pointerImageX = 250f,
            pointerImageY = 150f,
            imageWidth = 400,
            imageHeight = 300,
            aspect = ImageCropAspect.Free,
            minSize = 40,
        )
        assertEquals(0, shrunk.left)
        assertEquals(250, shrunk.right)
        val overflow = resizeCrop(
            crop = shrunk,
            handle = CropHandle.Right,
            pointerImageX = 900f,
            pointerImageY = 150f,
            imageWidth = 400,
            imageHeight = 300,
            aspect = ImageCropAspect.Free,
            minSize = 40,
        )
        assertEquals(400, overflow.right)
        assertEquals(0, overflow.left)
        assertTrue(overflow.top >= 0)
        assertTrue(overflow.bottom <= 300)
    }

    @Test
    fun `resize left handle clamps to zero and min size`() {
        val start = PixelRect(0, 0, 400, 300)
        val moved = resizeCrop(
            crop = start,
            handle = CropHandle.Left,
            pointerImageX = -80f,
            pointerImageY = 10f,
            imageWidth = 400,
            imageHeight = 300,
            aspect = ImageCropAspect.Free,
            minSize = 48,
        )
        assertEquals(0, moved.left)
        val tight = resizeCrop(
            crop = start,
            handle = CropHandle.Left,
            pointerImageX = 390f,
            pointerImageY = 10f,
            imageWidth = 400,
            imageHeight = 300,
            aspect = ImageCropAspect.Free,
            minSize = 48,
        )
        assertEquals(352, tight.left)
        assertEquals(400, tight.right)
        assertEquals(48, tight.width)
    }

    @Test
    fun `rotation recalculates free crop to the full rotated image`() {
        val landscape = cropAfterRotation(1600, 900, 0, ImageCropAspect.Free)
        assertEquals(1600, landscape.width)
        assertEquals(900, landscape.height)
        val portrait = cropAfterRotation(1600, 900, 90, ImageCropAspect.Free)
        assertEquals(0, portrait.left)
        assertEquals(0, portrait.top)
        assertEquals(900, portrait.right)
        assertEquals(1600, portrait.bottom)
        val applied = bitmapCropRect(
            imageWidth = 1600,
            imageHeight = 900,
            transform = ImageCropTransform(rotationDegrees = 90, aspect = ImageCropAspect.Free),
        )
        assertEquals(portrait, applied)
    }

    @Test
    fun `locked square resize keeps ratio and stays inside`() {
        val start = initialCropRect(400, 400, ImageCropAspect.Square)
        val resized = resizeCrop(
            crop = start,
            handle = CropHandle.BottomRight,
            pointerImageX = 200f,
            pointerImageY = 180f,
            imageWidth = 400,
            imageHeight = 400,
            aspect = ImageCropAspect.Square,
            minSize = 40,
        )
        assertEquals(resized.width, resized.height)
        assertTrue(resized.left >= 0)
        assertTrue(resized.top >= 0)
        assertTrue(resized.right <= 400)
        assertTrue(resized.bottom <= 400)
        assertTrue(resized.width < 400)
    }

    @Test
    fun `fit layout at scale 1 shows the whole landscape image`() {
        val layout = fittedImageLayout(
            viewportWidth = 400f,
            viewportHeight = 800f,
            rotatedWidth = 1600,
            rotatedHeight = 900,
            userScale = 1f,
            panX = 80f,
            panY = 80f,
            padding = 16f,
        )
        val imageRatio = 1600f / 900f
        val displayedRatio = layout.width / layout.height
        assertEquals(imageRatio, displayedRatio, 0.01f)
        assertTrue(layout.width <= 400f - 31f)
        assertTrue(layout.height < 800f)
        assertEquals((400f - layout.width) / 2f, layout.left, 0.5f)
    }

    @Test
    fun `handle hit prefers a corner inside the touch radius`() {
        val crop = PixelRect(0, 0, 100, 50)
        val layout = FittedImageLayout(drawScale = 2f, left = 10f, top = 20f, width = 200f, height = 100f)
        val handle = hitCropHandle(
            screenX = 10f,
            screenY = 20f,
            crop = crop,
            layout = layout,
            touchRadius = 48f,
        )
        assertEquals(CropHandle.TopLeft, handle)
        val miss = hitCropHandle(1000f, 1000f, crop, layout, 48f)
        assertEquals(null, miss)
        val right = hitCropHandle(210f, 70f, crop, layout, 48f)
        assertNotNull(right)
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
