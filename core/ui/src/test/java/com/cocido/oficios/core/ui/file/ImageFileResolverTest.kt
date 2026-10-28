package com.cocido.oficios.core.ui.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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

    @Test
    fun `content resolver uri of large transparent png becomes jpeg under 900 kb on white`() {
        val context = RuntimeEnvironment.getApplication()
        val width = 4000
        val height = 3000
        val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        source.eraseColor(Color.TRANSPARENT)
        for (x in 0 until 80) {
            for (y in 0 until 80) {
                source.setPixel(x, y, Color.RED)
            }
        }
        val png = File(context.cacheDir, "alpha-4000x3000.png")
        png.outputStream().use { stream ->
            assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        source.recycle()

        val decoded = decodeScaledBitmap(context, Uri.fromFile(png))
        checkNotNull(decoded)
        assertTrue("decode ${decoded.width}x${decoded.height}", decoded.width > 0)
        val corner = decoded.getPixel(decoded.width - 2, decoded.height - 2)
        val origin = decoded.getPixel(0, 0)
        assertTrue(
            "PNG alfa a blanco. origin=${Integer.toHexString(origin)} corner=${Integer.toHexString(corner)} ${decoded.width}x${decoded.height} alpha=${decoded.hasAlpha()}",
            Color.red(corner) > 200 && Color.green(corner) > 200 && Color.blue(corner) > 200,
        )
        decoded.recycle()
        val path = compressUriToJpegFile(context, Uri.fromFile(png), "cert")
        val out = File(path)
        assertTrue(out.exists())
        assertTrue(out.length() <= ImageUploadContract.TARGET_MAX_BYTES)
        val jpeg = BitmapFactory.decodeFile(path)
        assertTrue(jpeg != null)
        checkNotNull(jpeg)
        assertTrue(maxOf(jpeg.width, jpeg.height) <= ImageUploadContract.MAX_EDGE_PX)
        val transparentMapped = jpeg.getPixel(jpeg.width - 2, jpeg.height - 2)
        assertTrue(
            "PNG alfa debe aplanarse a blanco, no a negro. pixel=${Integer.toHexString(transparentMapped)}",
            Color.red(transparentMapped) > 200 &&
                Color.green(transparentMapped) > 200 &&
                Color.blue(transparentMapped) > 200,
        )
        jpeg.recycle()
    }

    @Test
    fun `content resolver uri of jpeg with exif rotate 90 is upright and under 900 kb`() {
        val context = RuntimeEnvironment.getApplication()
        val width = 400
        val height = 200
        val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                source.setPixel(x, y, if (x < width / 2) Color.RED else Color.BLUE)
            }
        }
        val jpegFile = File(context.cacheDir, "exif-rotated.jpg")
        jpegFile.outputStream().use { stream ->
            assertTrue(source.compress(Bitmap.CompressFormat.JPEG, 95, stream))
        }
        source.recycle()
        ExifInterface(jpegFile.absolutePath).apply {
            setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90.toString(),
            )
            saveAttributes()
        }

        val path = compressUriToJpegFile(context, Uri.fromFile(jpegFile), "perfil")
        val out = File(path)
        assertTrue(out.length() <= ImageUploadContract.TARGET_MAX_BYTES)
        val decoded = BitmapFactory.decodeFile(path)
        checkNotNull(decoded)
        assertTrue(
            "EXIF 90 debe intercambiar el aspecto 400x200 → alto > ancho",
            decoded.height >= decoded.width,
        )
        val top = decoded.getPixel(decoded.width / 2, 4)
        val bottom = decoded.getPixel(decoded.width / 2, decoded.height - 5)
        assertTrue(
            "Tras rotar 90, el rojo original (izquierda) queda arriba. top=${Integer.toHexString(top)}",
            Color.red(top) > Color.blue(top),
        )
        assertTrue(
            "Tras rotar 90, el azul original (derecha) queda abajo. bottom=${Integer.toHexString(bottom)}",
            Color.blue(bottom) > Color.red(bottom),
        )
        decoded.recycle()
    }

    @Test
    fun `failed decode surfaces usted copy never english`() {
        val technical = IllegalStateException("Cannot decode image")
        assertEquals(
            ImageUploadContract.IMAGE_READ_FAILED_MESSAGE,
            ImageUploadContract.userFacingFileFailure(technical),
        )
        assertTrue("Cannot" !in ImageUploadContract.IMAGE_READ_FAILED_MESSAGE)
        assertTrue("decode" !in ImageUploadContract.IMAGE_READ_FAILED_MESSAGE.lowercase())
    }
}
