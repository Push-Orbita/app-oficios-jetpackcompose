package com.cocido.oficios.core.ui.file

import android.graphics.Bitmap
import android.graphics.Color
import com.cocido.oficios.core.common.contract.ImageUploadContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageFileResolverTest {

    @Test
    fun `encodeUnderTarget writes jpeg below 900 kb`() {
        val size = 1600
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size) { index ->
            Color.rgb(index % 256, (index / 5) % 256, (index / 17) % 256)
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        val bytes = encodeUnderTarget(bitmap)
        if (!bitmap.isRecycled) bitmap.recycle()
        assertTrue(bytes.size >= 3)
        assertEquals(0xFF.toByte(), bytes[0])
        assertEquals(0xD8.toByte(), bytes[1])
        assertTrue(bytes.size <= ImageUploadContract.TARGET_MAX_BYTES.toInt())
    }
}
