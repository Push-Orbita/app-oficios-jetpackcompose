package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.profile.IdentityDocumentUploadDataSource
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityDocumentRemoteDataSource @Inject constructor(
    private val identityDocumentUploadDataSource: IdentityDocumentUploadDataSource,
) {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) = safeApiCall {
        identityDocumentUploadDataSource.uploadDniImages(frontImagePath, backImagePath)
    }
}
