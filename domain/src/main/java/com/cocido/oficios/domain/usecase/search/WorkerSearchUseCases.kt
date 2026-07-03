package com.cocido.oficios.domain.usecase.search

import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.repository.RecentSearchRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import javax.inject.Inject

class SearchWorkersUseCase @Inject constructor(
    private val workerSearchRepository: WorkerSearchRepository,
) {
    suspend operator fun invoke(
        filters: WorkerSearchFilters,
        pageNumber: Int,
        pageSize: Int = WorkerSearchRepository.DEFAULT_PAGE_SIZE,
    ) = workerSearchRepository.searchWorkers(filters, pageNumber, pageSize)
}

class TrackRecentSearchUseCase @Inject constructor(
    private val recentSearchRepository: RecentSearchRepository,
) {
    suspend operator fun invoke(entry: RecentSearchEntry) {
        recentSearchRepository.track(entry)
    }
}

class GetRecentSearchesUseCase @Inject constructor(
    private val recentSearchRepository: RecentSearchRepository,
) {
    suspend operator fun invoke(): List<RecentSearchEntry> = recentSearchRepository.getRecents()
}
