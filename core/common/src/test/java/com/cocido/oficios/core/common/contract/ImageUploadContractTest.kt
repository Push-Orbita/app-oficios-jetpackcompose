package com.cocido.oficios.core.common.contract

import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageUploadContractTest {

    @Test
    fun `image star is not a valid mime`() {
        assertNull(ImageUploadContract.normalizeMime("image/*"))
        assertNull(ImageUploadContract.normalizeMime("IMAGE/*"))
    }

    @Test
    fun `normalizes jpeg aliases`() {
        assertEquals("image/jpeg", ImageUploadContract.normalizeMime("image/jpg"))
        assertEquals("image/jpeg", ImageUploadContract.normalizeMime("image/pjpeg"))
        assertEquals("image/png", ImageUploadContract.normalizeMime("image/png; charset=binary"))
    }

    @Test
    fun `magic bytes detect jpeg png gif webp`() {
        assertEquals(
            "image/jpeg",
            ImageUploadContract.mimeFromMagicBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())),
        )
        assertEquals(
            "image/png",
            ImageUploadContract.mimeFromMagicBytes(
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
            ),
        )
        assertEquals("image/gif", ImageUploadContract.mimeFromMagicBytes("GIF89a".toByteArray()))
        val webp = ByteArray(12)
        "RIFF".toByteArray().copyInto(webp, 0)
        "WEBP".toByteArray().copyInto(webp, 8)
        assertEquals("image/webp", ImageUploadContract.mimeFromMagicBytes(webp))
    }

    @Test
    fun `requireAcceptedMime prefers magic over declared image star`() {
        val file = File.createTempFile("sample", ".bin").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        assertEquals("image/jpeg", ImageUploadContract.requireAcceptedMime(file, "image/*"))
        file.delete()
    }

    @Test(expected = ClientValidationException::class)
    fun `requireAcceptedMime rejects unknown bytes`() {
        val file = File.createTempFile("sample", ".bin").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        try {
            ImageUploadContract.requireAcceptedMime(file, "image/*")
        } finally {
            file.delete()
        }
    }

    @Test
    fun `gallery file name uses galeria prefix and concrete extension`() {
        val name = ImageUploadContract.galleryFileName("image/png", index = 2)
        assertTrue(name.startsWith("galeria_"))
        assertTrue(name.endsWith("_2.png"))
        assertTrue("dni_" !in name)
    }

    @Test
    fun `certificate copy names the real upload limit not 10 MB`() {
        assertTrue("10 MB" !in ImageUploadContract.CERTIFICATE_FORMATS_HINT)
        assertTrue("900 KB" in ImageUploadContract.CERTIFICATE_FORMATS_HINT)
        assertTrue("1 MB" in ImageUploadContract.CERTIFICATE_FORMATS_HINT)
    }

    @Test
    fun `pdf magic and size helpers`() {
        assertTrue(ImageUploadContract.isPdfMagic("%PDF-1.4".toByteArray()))
        assertTrue(ImageUploadContract.exceedsPdfLimit(ImageUploadContract.PDF_MAX_BYTES + 1))
        assertTrue(!ImageUploadContract.exceedsPdfLimit(ImageUploadContract.PDF_MAX_BYTES))
        assertTrue(ImageUploadContract.exceedsImageTarget(ImageUploadContract.TARGET_MAX_BYTES + 1))
        assertTrue(!ImageUploadContract.exceedsImageTarget(ImageUploadContract.TARGET_MAX_BYTES))
        assertEquals("900 KB", ImageUploadContract.formatByteSize(900L * 1024L))
    }

    @Test
    fun `technical decode errors never reach the user copy`() {
        assertEquals(
            ImageUploadContract.IMAGE_READ_FAILED_MESSAGE,
            ImageUploadContract.userFacingFileFailure(IllegalStateException("Cannot decode image")),
        )
        assertEquals(
            ImageUploadContract.IMAGE_READ_FAILED_MESSAGE,
            ImageUploadContract.userFacingFileFailure(IllegalStateException("Cannot compress image")),
        )
        assertEquals(
            ImageUploadContract.PDF_TOO_LARGE_MESSAGE,
            ImageUploadContract.userFacingFileFailure(
                com.cocido.oficios.core.common.result.ClientValidationException(
                    ImageUploadContract.PDF_TOO_LARGE_MESSAGE,
                ),
            ),
        )
    }
}
