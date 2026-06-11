package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDto
import com.cocido.oficios.core.data.remote.ProviderProfileRemoteDataSource
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProviderProfileRemoteDataSource,
) : ProviderProfileRepository {

    override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit> =
        remoteDataSource.updateProfessionalInfo(request.toDto()).map { }

    override suspend fun submitRubroFull(request: ProviderRubroFullRequest): AppResult<Unit> =
        remoteDataSource.createRubroFull(request.toDto()).map { }

    override suspend fun finalizeProfile(): AppResult<Unit> =
        remoteDataSource.finalizeProfile().map { }
}
