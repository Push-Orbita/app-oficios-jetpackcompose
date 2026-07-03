package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.mapper.toPagedWorkers
import com.cocido.oficios.core.data.remote.WorkerSearchRemoteDataSource
import com.cocido.oficios.core.network.safeApiCall
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class WorkerSearchRepositoryImpl @Inject constructor(
    private val remoteDataSource: WorkerSearchRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : WorkerSearchRepository {

    override suspend fun searchWorkers(
        filters: WorkerSearchFilters,
        pageNumber: Int,
        pageSize: Int,
    ): AppResult<PagedWorkers> = withContext(dispatcherProvider.io) {
        safeApiCall {
            remoteDataSource.searchWorkers(filters, pageNumber, pageSize).toPagedWorkers(filters)
        }
    }
}
