package com.cocido.oficios.core.ui.gallery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryMediaTest {

    @Test
    fun pdfUrlsAreDetectedEvenWithQueryAndFragment() {
        assertEquals(GalleryMediaKind.Pdf, "https://cdn.example/file.PDF".toGalleryMediaKind())
        assertEquals(GalleryMediaKind.Pdf, "https://cdn.example/doc.pdf?token=1#page=2".toGalleryMediaKind())
        assertEquals(GalleryMediaKind.Image, "https://cdn.example/photo.jpg".toGalleryMediaKind())
        assertEquals(GalleryMediaKind.Image, null.toGalleryMediaKind())
    }

    @Test
    fun viewableImagesExcludeBlankAndPdf() {
        assertTrue("https://cdn.example/a.webp".isViewableImage())
        assertFalse("https://cdn.example/a.pdf".isViewableImage())
        assertFalse("".isViewableImage())
        assertFalse(null.isViewableImage())
    }

    @Test
    fun mosaicSliceComputesVisibleHiddenAndOverflow() {
        assertEquals(MosaicSlice(0, 0), mosaicSlice(total = 0, previewLimit = 5))
        assertEquals(MosaicSlice(3, 0), mosaicSlice(total = 3, previewLimit = 5))
        assertEquals(MosaicSlice(5, 3), mosaicSlice(total = 8, previewLimit = 5))
        assertEquals(MosaicSlice(8, 0), mosaicSlice(total = 8, previewLimit = null))
        assertTrue(mosaicSlice(8, 5).hasOverflow)
        assertFalse(mosaicSlice(2, 5).hasOverflow)
    }
}
