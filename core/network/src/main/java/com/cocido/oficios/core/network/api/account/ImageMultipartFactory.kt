package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.common.contract.ImageUploadContract
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

internal fun createGalleryPhotoPart(file: File, index: Int = 0): MultipartBody.Part {
    val mime = ImageUploadContract.requireAcceptedMime(file)
    return createTypedImagePart(
        fieldName = "fotos",
        fileName = ImageUploadContract.galleryFileName(mime, index),
        file = file,
        mime = mime,
    )
}

internal fun createProfilePhotoPart(file: File): MultipartBody.Part {
    val mime = ImageUploadContract.requireAcceptedMime(file)
    return createTypedImagePart(
        fieldName = "fotoPerfil",
        fileName = ImageUploadContract.profileFileName(mime),
        file = file,
        mime = mime,
    )
}

internal fun createCertificateFilePart(fieldName: String, file: File): MultipartBody.Part {
    val mime = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        else -> ImageUploadContract.requireAcceptedMime(file)
    }
    return MultipartBody.Part.createFormData(
        fieldName,
        ImageUploadContract.certificateFileName(mime, file.name),
        file.asRequestBody(mime.toMediaType()),
    )
}

internal fun createTypedImagePart(
    fieldName: String,
    fileName: String,
    file: File,
    mime: String,
): MultipartBody.Part = MultipartBody.Part.createFormData(
    fieldName,
    fileName,
    file.asRequestBody(mime.toMediaType()),
)
