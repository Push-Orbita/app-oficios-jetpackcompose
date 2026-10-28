package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkGalleryUploadPartTest {

    @Test
    fun `gallery part uses fotos field concrete jpeg mime and galeria file name`() {
        val file = File.createTempFile("dni_3294475402739172456", ".jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        val part = createGalleryPhotoPart(file, index = 0)
        val disposition = part.headers?.get("Content-Disposition").orEmpty()
        assertTrue(disposition.contains("name=\"fotos\""))
        assertTrue(disposition.contains("filename=\"galeria_"))
        assertTrue(disposition.contains(".jpg\""))
        assertTrue("dni_" !in disposition)
        assertEquals("image/jpeg", part.body.contentType()?.toString())
        file.delete()
    }

    @Test
    fun `profile part does not send image star`() {
        val file = File.createTempFile("photo", ".png").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        }
        val part = createProfilePhotoPart(file)
        assertEquals("image/png", part.body.contentType()?.toString())
        assertTrue(part.headers?.get("Content-Disposition").orEmpty().contains("filename=\"perfil_"))
        file.delete()
    }

    @Test
    fun `certificate image part uses concrete mime`() {
        val file = File.createTempFile("scan", ".png").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        }
        val part = createCertificateFilePart("certificadoArchivo", file)
        assertEquals("image/png", part.body.contentType()?.toString())
        assertTrue("image/*" != part.body.contentType()?.toString())
        file.delete()
    }

    @Test(expected = ClientValidationException::class)
    fun `rejects bytes that are not an accepted image`() {
        val file = File.createTempFile("payload", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        try {
            createGalleryPhotoPart(file)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `part body is the original file bytes`() {
        val payload = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x11)
        val file = File.createTempFile("payload", ".jpg").apply { writeBytes(payload) }
        val part = createGalleryPhotoPart(file)
        val buffer = Buffer()
        part.body.writeTo(buffer)
        assertEquals(payload.toList(), buffer.readByteArray().toList())
        file.delete()
    }
}
