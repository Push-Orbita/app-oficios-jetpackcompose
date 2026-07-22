package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.profile.IdentityDocumentApi
import com.cocido.oficios.core.network.api.profile.IdentityDocumentUploadDataSource
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityDocumentRemoteDataSource @Inject constructor(
    private val identityDocumentUploadDataSource: IdentityDocumentUploadDataSource,
    private val identityDocumentApi: IdentityDocumentApi,
) {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) = safeApiCall {
        identityDocumentUploadDataSource.uploadDniImages(frontImagePath, backImagePath)
    }

    suspend fun uploadSelfie(imagePath: String) = safeApiCall {
        identityDocumentUploadDataSource.uploadSelfie(imagePath)
    }

    suspend fun replaceSelfie(documentId: Long, imagePath: String) = safeApiCall {
        identityDocumentUploadDataSource.replaceSelfie(documentId, imagePath)
    }

    suspend fun searchDocuments(tipoDocumento: String? = null) = safeApiCall {
        identityDocumentApi.searchDocuments(tipoDocumento = tipoDocumento)
    }
}
