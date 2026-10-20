package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.mapper.toPagedWorkerReviews
import com.cocido.oficios.core.data.mapper.toWorkerProfile
import com.cocido.oficios.core.data.remote.WorkerProfileRemoteDataSource
import com.cocido.oficios.core.network.safeApiCall
import com.cocido.oficios.domain.model.PagedWorkerReviews
import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.repository.WorkerProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class WorkerProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: WorkerProfileRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : WorkerProfileRepository {

    override suspend fun getWorkerProfile(workerId: Long): AppResult<WorkerProfile> =
        withContext(dispatcherProvider.io) {
            safeApiCall {
                remoteDataSource.getWorkerProfile(workerId).toWorkerProfile()
            }
        }

    override suspend fun getWorkerReviews(
        workerId: Long,
        pageNumber: Int,
        pageSize: Int,
    ): AppResult<PagedWorkerReviews> = withContext(dispatcherProvider.io) {
        safeApiCall {
            remoteDataSource.searchWorkerReviews(
                workerId = workerId,
                pageNumber = pageNumber,
                pageSize = pageSize,
            ).toPagedWorkerReviews(pageSize = pageSize)
        }
    }
}

