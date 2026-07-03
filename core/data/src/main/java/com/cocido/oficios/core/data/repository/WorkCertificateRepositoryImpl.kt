package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toWorkCertificateList
import com.cocido.oficios.core.data.remote.WorkCertificateRemoteDataSource
import com.cocido.oficios.domain.repository.WorkCertificateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkCertificateRepositoryImpl @Inject constructor(
    private val remoteDataSource: WorkCertificateRemoteDataSource,
) : WorkCertificateRepository {

    override suspend fun uploadCertificate(name: String, filePath: String) =
        remoteDataSource.uploadCertificate(name, filePath).map { it.toDomain() }

    override suspend fun getMyCertificates() =
        remoteDataSource.searchCertificates().map { it.toWorkCertificateList() }
}
