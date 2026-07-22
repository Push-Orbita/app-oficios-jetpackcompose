package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toProviderDniDocuments
import com.cocido.oficios.core.data.mapper.toProviderSelfieDocument
import com.cocido.oficios.core.data.remote.IdentityDocumentRemoteDataSource
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityDocumentRepositoryImpl @Inject constructor(
    private val remoteDataSource: IdentityDocumentRemoteDataSource,
) : IdentityDocumentRepository {

    override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) =
        remoteDataSource.uploadDniImages(frontImagePath, backImagePath).map { }

    override suspend fun uploadSelfie(imagePath: String) =
        remoteDataSource.uploadSelfie(imagePath).map { }

    override suspend fun replaceSelfie(documentId: Long, imagePath: String) =
        remoteDataSource.replaceSelfie(documentId, imagePath).map { }

    override suspend fun getMyDniDocuments() =
        remoteDataSource.searchDocuments().map { it.toProviderDniDocuments() }

    override suspend fun getMySelfieDocument() =
        remoteDataSource.searchDocuments(tipoDocumento = "selfie").map { it.toProviderSelfieDocument() }
}
