package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.remote.ReportsRemoteDataSource
import com.cocido.oficios.domain.repository.ReportsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReportsRemoteDataSource,
) : ReportsRepository {

    override suspend fun getReasons() =
        remoteDataSource.getReasons().map { response -> response.data.map { it.toDomain() } }

    override suspend fun submit(
        workerId: Long,
        reasonCode: String,
        description: String,
        idempotencyKey: String,
        bookingId: Long?,
    ) = remoteDataSource.submit(
        workerId = workerId,
        reasonCode = reasonCode,
        description = description,
        idempotencyKey = idempotencyKey,
        bookingId = bookingId,
    ).map { it.toDomain() }
}
