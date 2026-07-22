package com.cocido.oficios.domain.usecase.worker

import com.cocido.oficios.domain.repository.WorkerProfileRepository
import javax.inject.Inject

class GetWorkerProfileUseCase @Inject constructor(
    private val workerProfileRepository: WorkerProfileRepository,
) {
    suspend operator fun invoke(workerId: Long) =
        workerProfileRepository.getWorkerProfile(workerId)
}

class GetWorkerReviewsUseCase @Inject constructor(
    private val workerProfileRepository: WorkerProfileRepository,
) {
    suspend operator fun invoke(
        workerId: Long,
        pageNumber: Int,
        pageSize: Int,
    ) = workerProfileRepository.getWorkerReviews(
        workerId = workerId,
        pageNumber = pageNumber,
        pageSize = pageSize,
    )
}

