package com.cocido.oficios.core.network.api.profile

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class IdentityDocumentUploadDataSource @Inject constructor(
    private val identityDocumentApi: IdentityDocumentApi,
) {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) {
        val frontFile = File(frontImagePath)
        val backFile = File(backImagePath)
        val mediaType = "image/*".toMediaTypeOrNull()
        identityDocumentApi.uploadDniDocuments(
            frenteArchivo = MultipartBody.Part.createFormData(
                "frenteArchivo",
                frontFile.name,
                frontFile.asRequestBody(mediaType),
            ),
            dorsoArchivo = MultipartBody.Part.createFormData(
                "dorsoArchivo",
                backFile.name,
                backFile.asRequestBody(mediaType),
            ),
        )
    }
}
