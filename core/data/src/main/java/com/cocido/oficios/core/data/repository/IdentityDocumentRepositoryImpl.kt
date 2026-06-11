package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.remote.IdentityDocumentRemoteDataSource
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityDocumentRepositoryImpl @Inject constructor(
    private val remoteDataSource: IdentityDocumentRemoteDataSource,
) : IdentityDocumentRepository {

    override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String): AppResult<Unit> =
        remoteDataSource.uploadDniImages(frontImagePath, backImagePath).map { }
}
