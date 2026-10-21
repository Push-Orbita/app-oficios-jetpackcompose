package com.cocido.oficios.core.ui.file

import android.net.Uri
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
class DocumentFileResolverTest {

    @Test
    fun `pdf over 1 mb is rejected with clear copy`() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "grande.pdf").apply {
            outputStream().use { stream ->
                stream.write("%PDF".toByteArray())
                stream.write(ByteArray((1024 * 1024) + 64) { 1 })
            }
        }
        val pick = pickCertificateFile(context, Uri.fromFile(file))
        assertTrue(pick is CertificateFilePick.Rejected)
        assertEquals(
            ImageUploadContract.PDF_TOO_LARGE_MESSAGE,
            (pick as CertificateFilePick.Rejected).message,
        )
    }

    @Test
    fun `png certificate via uri is compressed never cannot decode`() {
        val context = RuntimeEnvironment.getApplication()
        val bitmap = android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
        bitmap.setPixel(0, 0, android.graphics.Color.GREEN)
        val png = File(context.cacheDir, "cert.png")
        png.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val pick = pickCertificateFile(context, Uri.fromFile(png))
        assertTrue(pick is CertificateFilePick.Ready)
        val ready = pick as CertificateFilePick.Ready
        assertTrue(!ready.isPdf)
        assertTrue(ready.sizeBytes <= ImageUploadContract.TARGET_MAX_BYTES)
        assertTrue("Cannot" !in ready.displayName)
    }

    @Test
    fun `small pdf is accepted`() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.cacheDir, "ok.pdf").apply {
            writeBytes("%PDF-1.4 small".toByteArray())
        }
        val pick = pickCertificateFile(context, Uri.fromFile(file))
        assertTrue(pick is CertificateFilePick.Ready)
        val ready = pick as CertificateFilePick.Ready
        assertTrue(ready.isPdf)
        assertTrue(ready.sizeBytes > 0L)
    }
}
