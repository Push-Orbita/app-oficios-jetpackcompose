package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.common.contract.DniImageUploadContract
import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DniImageUploadTest {

    @Test
    fun `createDniImagePart uses backend field names and detects mime from magic bytes`() {
        val file = File.createTempFile("dni_front", ".jpg").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        }

        val part = createDniImagePart(
            fieldName = DniImageUploadContract.PART_FRONT_FIELD,
            fileNamePrefix = DniImageUploadContract.FRONT_FILE_PREFIX,
            file = file,
        )

        assertEquals(DniImageUploadContract.PART_FRONT_FIELD, part.headers?.get("Content-Disposition")?.let {
            it.substringAfter("name=\"").substringBefore("\"")
        })
        assertEquals("image/png", part.body.contentType()?.toString())
        assertTrue(part.headers?.get("Content-Disposition")?.contains("${DniImageUploadContract.FRONT_FILE_PREFIX}_") == true)
        assertTrue(part.headers?.get("Content-Disposition")?.contains(".png") == true)

        file.delete()
    }

    @Test(expected = ClientValidationException::class)
    fun `createDniImagePart rejects unsupported file format`() {
        val file = File.createTempFile("dni_front", ".heic").apply {
            writeBytes(byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69, 0x63))
        }
        try {
            createDniImagePart(
                fieldName = DniImageUploadContract.PART_FRONT_FIELD,
                fileNamePrefix = DniImageUploadContract.FRONT_FILE_PREFIX,
                file = file,
            )
        } finally {
            file.delete()
        }
    }

    @Test(expected = ClientValidationException::class)
    fun `createDniImagePart rejects empty file`() {
        val file = File.createTempFile("dni_front", ".jpg")
        try {
            createDniImagePart(
                fieldName = DniImageUploadContract.PART_BACK_FIELD,
                fileNamePrefix = DniImageUploadContract.BACK_FILE_PREFIX,
                file = file,
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun `createSelfieImagePart uses correct field name and detects mime`() {
        val file = File.createTempFile("selfie", ".jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }

        val part = createSelfieImagePart(file)

        assertEquals("selfieArchivo", part.headers?.get("Content-Disposition")?.let {
            it.substringAfter("name=\"").substringBefore("\"")
        })
        assertEquals("image/jpeg", part.body.contentType()?.toString())
        assertTrue(part.headers?.get("Content-Disposition")?.contains("selfie_") == true)

        file.delete()
    }
}
