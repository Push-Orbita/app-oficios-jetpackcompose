package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.common.contract.DniImageUploadContract
import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

private const val SELFIE_PART_FIELD = "selfieArchivo"
private const val SELFIE_FILE_PREFIX = "selfie"

internal fun createSelfieImagePart(file: File): MultipartBody.Part {
    val mime = ImageUploadContract.requireAcceptedMime(file)
    val extension = ImageUploadContract.extensionForMime(mime) ?: "jpg"
    return MultipartBody.Part.createFormData(
        SELFIE_PART_FIELD,
        "${SELFIE_FILE_PREFIX}_${System.currentTimeMillis()}.$extension",
        file.asRequestBody(mime.toMediaType()),
    )
}

internal fun createDniImagePart(
    fieldName: String,
    fileNamePrefix: String,
    file: File,
): MultipartBody.Part {
    val mime = ImageUploadContract.requireAcceptedMime(file)
    val extension = ImageUploadContract.extensionForMime(mime) ?: "jpg"
    return MultipartBody.Part.createFormData(
        fieldName,
        "${fileNamePrefix}_${System.currentTimeMillis()}.$extension",
        file.asRequestBody(mime.toMediaType()),
    )
}
