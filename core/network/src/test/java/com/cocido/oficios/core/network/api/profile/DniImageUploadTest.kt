package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.common.contract.DniImageUploadContract
import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DniImageUploadTest {

    @Test
    fun `createDniImagePart uses backend field names and specific mime type`() {
        val file = File.createTempFile("dni_front", ".png").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47))
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
        assertTrue(part.headers?.get("Content-Disposition")?.contains("dni_frente.png") == true)

        file.delete()
    }

    @Test(expected = ClientValidationException::class)
    fun `validateDniImageFile rejects unsupported extension`() {
        val file = File.createTempFile("dni_front", ".heic").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        try {
            validateDniImageFile(file, sideLabel = "frente")
        } finally {
            file.delete()
        }
    }

    @Test(expected = ClientValidationException::class)
    fun `validateDniImageFile rejects empty file`() {
        val file = File.createTempFile("dni_front", ".jpg")
        try {
            validateDniImageFile(file, sideLabel = "dorso")
        } finally {
            file.delete()
        }
    }
}
