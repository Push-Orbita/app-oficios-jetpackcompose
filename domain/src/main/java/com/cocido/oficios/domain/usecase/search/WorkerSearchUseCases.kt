package com.cocido.oficios.domain.usecase.search

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.getOrNull
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.RecentSearchEntry
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.model.normalizeSearchText
import com.cocido.oficios.domain.model.recentClientWorkers
import com.cocido.oficios.domain.model.withResolvedTrade
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.OrderRepository
import com.cocido.oficios.domain.repository.RecentSearchRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Busca trabajadores por nombre (dentro o fuera de un rubro) y por oficio.
 *
 * `GET /trabajador-oficio/search?q=` filtra con un solo `LIKE %q%` sobre `nombres`,
 * `apellido`, email o documento, combinable con `rubroId`/`subRubroId`. Por eso:
 * - El texto primero se busca como nombre, respetando el rubro elegido si hay uno.
 * - Un nombre completo ("Facundo Barberis") no matchea ningún campo solo; se manda la
 *   palabra más larga como `q` y se exige el resto en el nombre del lado del cliente.
 * - Si no hay nadie con ese nombre y no hay rubro elegido, el texto se interpreta como
 *   oficio ("plom" → Plomería), como antes.
 */
class SearchWorkersUseCase @Inject constructor(
    private val workerSearchRepository: WorkerSearchRepository,
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(
        filters: WorkerSearchFilters,
        pageNumber: Int,
        pageSize: Int = WorkerSearchRepository.DEFAULT_PAGE_SIZE,
    ): AppResult<PagedWorkers> {
        val query = filters.query?.trim().orEmpty()
        if (query.isEmpty()) {
            return workerSearchRepository.searchWorkers(filters.copy(query = null), pageNumber, pageSize)
        }
        val byName = searchByName(filters, query, pageNumber, pageSize)
        val hasTradeFilter = filters.rubroId != null || filters.subRubroId != null
        if (hasTradeFilter) return byName
        if (byName is AppResult.Success && byName.data.items.isNotEmpty()) return byName

        val resolved = resolveTradeFilters(filters)
        if (resolved == filters) return byName
        return workerSearchRepository.searchWorkers(resolved, pageNumber, pageSize)
    }

    private suspend fun searchByName(
        filters: WorkerSearchFilters,
        query: String,
        pageNumber: Int,
        pageSize: Int,
    ): AppResult<PagedWorkers> {
        val tokens = nameTokens(query)
        if (tokens.size <= 1) {
            return workerSearchRepository.searchWorkers(filters.copy(query = query), pageNumber, pageSize)
        }
        val anchor = tokens.maxBy { it.length }
        val result = workerSearchRepository.searchWorkers(
            filters.copy(query = anchor),
            pageNumber,
            maxOf(pageSize, MULTI_WORD_PAGE_SIZE),
        )
        return when (result) {
            is AppResult.Success -> AppResult.Success(
                result.data.copy(items = result.data.items.filter { it.matchesAllTokens(tokens) }),
            )
            is AppResult.Error -> result
        }
    }

    private suspend fun resolveTradeFilters(filters: WorkerSearchFilters): WorkerSearchFilters {
        val query = filters.query?.trim().orEmpty()
        if (query.length < 3 || filters.rubroId != null || filters.subRubroId != null) return filters
        val rubros = catalogRepository.getAllRubros().getOrNull().orEmpty()
        val subRubros = catalogRepository.getAllSubRubros().getOrNull().orEmpty()
        return filters.withResolvedTrade(rubros = rubros, subRubros = subRubros)
    }

    private companion object {
        const val MULTI_WORD_PAGE_SIZE = 50
    }
}

internal fun nameTokens(query: String): List<String> =
    query.trim()
        .split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }

private fun WorkerSummary.matchesAllTokens(tokens: List<String>): Boolean {
    val name = normalizeSearchText(displayName)
    return tokens.all { name.contains(normalizeSearchText(it)) }
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
