package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.worker.WorkerProfileApi
import com.cocido.oficios.core.network.api.worker.WorkerReviewsApi
import com.cocido.oficios.core.network.api.worker.dto.TrabajadorOficioPerfilCompletoDto
import javax.inject.Inject

class WorkerProfileRemoteDataSource @Inject constructor(
    private val workerProfileApi: WorkerProfileApi,
    private val workerReviewsApi: WorkerReviewsApi,
) {
    suspend fun getWorkerProfile(workerId: Long): TrabajadorOficioPerfilCompletoDto =
        workerProfileApi.getWorkerProfile(workerId)

    suspend fun searchWorkerReviews(
        workerId: Long,
        pageNumber: Int,
        pageSize: Int,
    ) = workerReviewsApi.searchReviews(
        trabajadorOficioId = workerId,
        pageNumber = pageNumber,
        pageSize = pageSize,
    )
}

