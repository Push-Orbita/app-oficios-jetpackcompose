package com.cocido.oficios.core.ui.crop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageCropApplierTest {

    @Test
    fun `apply crop rotation yields jpeg under 900 kb`() {
        val context = RuntimeEnvironment.getApplication()
        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(30, 120, 200))
        val source = File(context.cacheDir, "crop-source.jpg")
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        bitmap.recycle()
        val path = applyImageCrop(
            context = context,
            sourcePath = source.absolutePath,
            transform = ImageCropTransform(rotationDegrees = 90, aspect = ImageCropAspect.Square),
            viewportWidth = 400f,
            viewportHeight = 400f,
            filePrefix = "crop",
        )
        val out = File(path)
        assertTrue(out.exists())
        assertTrue(out.length() <= ImageUploadContract.TARGET_MAX_BYTES)
        assertTrue(out.name.endsWith(".jpg"))
        val cropped = BitmapFactory.decodeFile(path)
        checkNotNull(cropped)
        assertTrue(
            "square crop after 90° ${cropped.width}x${cropped.height}",
            kotlin.math.abs(cropped.width - cropped.height) <= 2,
        )
        cropped.recycle()
    }

    @Test
    fun `ninety degree rotation maps left strip to the top`() {
        val context = RuntimeEnvironment.getApplication()
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888)
        for (x in 0 until 120) {
            for (y in 0 until 80) {
                bitmap.setPixel(x, y, if (x < 60) Color.RED else Color.BLUE)
            }
        }
        val source = File(context.cacheDir, "crop-rotate.jpg")
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val path = applyImageCrop(
            context = context,
            sourcePath = source.absolutePath,
            transform = ImageCropTransform(rotationDegrees = 90, aspect = ImageCropAspect.Free),
            viewportWidth = 80f,
            viewportHeight = 120f,
            filePrefix = "rotate",
        )
        val cropped = BitmapFactory.decodeFile(path)
        checkNotNull(cropped)
        assertEquals(80, cropped.width)
        assertEquals(120, cropped.height)
        val top = cropped.getPixel(cropped.width / 2, 4)
        val bottom = cropped.getPixel(cropped.width / 2, cropped.height - 5)
        assertTrue("top should be former left (red) ${Integer.toHexString(top)}", Color.red(top) > 180)
        assertTrue("bottom should be former right (blue) ${Integer.toHexString(bottom)}", Color.blue(bottom) > 180)
        cropped.recycle()
    }
}
