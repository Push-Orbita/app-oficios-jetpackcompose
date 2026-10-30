package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toProviderDniDocuments
import com.cocido.oficios.core.data.mapper.toProviderSelfieDocument
import com.cocido.oficios.core.data.remote.IdentityDocumentRemoteDataSource
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityDocumentRepositoryImpl @Inject constructor(
    private val remoteDataSource: IdentityDocumentRemoteDataSource,
    private val accountRepository: AccountRepository,
) : IdentityDocumentRepository {

    override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) =
        remoteDataSource.uploadDniImages(frontImagePath, backImagePath).map { }

    override suspend fun uploadSelfie(imagePath: String) =
        remoteDataSource.uploadSelfie(imagePath).map { }

    override suspend fun replaceSelfie(documentId: Long, imagePath: String) =
        remoteDataSource.replaceSelfie(documentId, imagePath).map { }

    override suspend fun replaceDni(
        documentId: Long,
        frontImagePath: String?,
        backImagePath: String?,
    ) = remoteDataSource.replaceDni(documentId, frontImagePath, backImagePath).map { }

    override suspend fun deleteDocument(documentId: Long) =
        remoteDataSource.deleteDocument(documentId).map { }

    override suspend fun getMyDniDocuments(): AppResult<com.cocido.oficios.domain.model.ProviderDniDocuments?> {
        val workerId = accountRepository.providerDetails.value?.workerId
        if (workerId == null) {
            return AppResult.Success(null)
        }
        return remoteDataSource.searchDocuments(
            tipoDocumento = "dni",
            trabajadorOficioId = workerId,
        ).map { it.toProviderDniDocuments() }
    }

    override suspend fun getMySelfieDocument(): AppResult<com.cocido.oficios.domain.model.ProviderSelfieDocument?> {
        val workerId = accountRepository.providerDetails.value?.workerId
        if (workerId == null) {
            return AppResult.Success(null)
        }
        return remoteDataSource.searchDocuments(
            tipoDocumento = "selfie",
            trabajadorOficioId = workerId,
        ).map { it.toProviderSelfieDocument() }
    }
}
