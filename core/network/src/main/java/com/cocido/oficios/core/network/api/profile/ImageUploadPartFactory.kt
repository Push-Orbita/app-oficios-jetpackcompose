package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.common.contract.DniImageUploadContract
import com.cocido.oficios.core.common.result.ClientValidationException
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

private const val SELFIE_PART_FIELD = "selfieArchivo"
private const val SELFIE_FILE_PREFIX = "selfie"

internal fun validateDniImageFile(file: File, sideLabel: String) {
    if (!file.exists() || file.length() == 0L) {
        throw ClientValidationException("La imagen del $sideLabel está vacía. Volvé a cargarla.")
    }

    val extension = file.extension
    if (!DniImageUploadContract.isAcceptedExtension(extension)) {
        throw ClientValidationException(
            "Formato no válido para $sideLabel. Aceptados: ${DniImageUploadContract.ACCEPTED_FORMATS_LABEL}",
        )
    }
}

internal fun createSelfieImagePart(file: File): MultipartBody.Part {
    validateDniImageFile(file, sideLabel = "selfie")
    return createDniImagePart(
        fieldName = SELFIE_PART_FIELD,
        fileNamePrefix = SELFIE_FILE_PREFIX,
        file = file,
    )
}

internal fun createDniImagePart(
    fieldName: String,
    fileNamePrefix: String,
    file: File,
): MultipartBody.Part {
    val normalizedExtension = DniImageUploadContract.normalizeExtension(
        file.extension.ifBlank { "jpg" },
    )
    val mediaType = DniImageUploadContract.mimeTypeForExtension(normalizedExtension).toMediaType()

    return MultipartBody.Part.createFormData(
        fieldName,
        "$fileNamePrefix.$normalizedExtension",
        file.asRequestBody(mediaType),
    )
}
