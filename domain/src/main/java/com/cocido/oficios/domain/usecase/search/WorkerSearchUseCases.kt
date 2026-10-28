package com.cocido.oficios.domain.usecase.search

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.getOrNull
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.model.recentClientWorkers
import com.cocido.oficios.domain.model.withResolvedTrade
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.repository.RecentSearchRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchWorkersUseCase @Inject constructor(
    private val workerSearchRepository: WorkerSearchRepository,
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(
        filters: WorkerSearchFilters,
        pageNumber: Int,
        pageSize: Int = WorkerSearchRepository.DEFAULT_PAGE_SIZE,
    ) = workerSearchRepository.searchWorkers(resolveTradeFilters(filters), pageNumber, pageSize)

    private suspend fun resolveTradeFilters(filters: WorkerSearchFilters): WorkerSearchFilters {
        val query = filters.query?.trim().orEmpty()
        if (query.length < 3 || filters.rubroId != null || filters.subRubroId != null) return filters
        val rubros = catalogRepository.getAllRubros().getOrNull().orEmpty()
        val subRubros = catalogRepository.getAllSubRubros().getOrNull().orEmpty()
        return filters.withResolvedTrade(rubros = rubros, subRubros = subRubros)
    }
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

class ObserveRecentSearchesUseCase @Inject constructor(
    private val recentSearchRepository: RecentSearchRepository,
) {
    operator fun invoke(): Flow<List<RecentSearchEntry>> = recentSearchRepository.observeRecents()
}

class RemoveRecentSearchUseCase @Inject constructor(
    private val recentSearchRepository: RecentSearchRepository,
) {
    suspend operator fun invoke(entry: RecentSearchEntry) {
        recentSearchRepository.remove(entry)
    }
}

class GetRecentClientWorkersUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(limit: Int = 8): List<WorkerSummary> {
        return when (val result = orderRepository.getClientOrders()) {
            is AppResult.Success -> result.data.recentClientWorkers(limit)
            is AppResult.Error -> emptyList()
        }
    }
}
