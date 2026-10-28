package com.cocido.oficios.domain.usecase.search

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchWorkersUseCaseTest {

    @Test
    fun `plom becomes rubroId and does not send q`() = runTest {
        val repository = RecordingSearchRepository()
        val useCase = SearchWorkersUseCase(repository, FakeTradeCatalogRepository())

        useCase(WorkerSearchFilters(query = "plom"), pageNumber = 1)

        val sent = repository.lastFilters
        assertEquals(1L, sent?.rubroId)
        assertEquals("Plomería", sent?.rubroLabel)
        assertNull(sent?.query)
    }

    @Test
    fun `unknown query stays as q`() = runTest {
        val repository = RecordingSearchRepository()
        val useCase = SearchWorkersUseCase(repository, FakeTradeCatalogRepository())

        useCase(WorkerSearchFilters(query = "Carlos"), pageNumber = 1)

        assertEquals("Carlos", repository.lastFilters?.query)
        assertNull(repository.lastFilters?.rubroId)
    }

    private class RecordingSearchRepository : WorkerSearchRepository {
        var lastFilters: WorkerSearchFilters? = null
        override suspend fun searchWorkers(
            filters: WorkerSearchFilters,
            pageNumber: Int,
            pageSize: Int,
        ): AppResult<PagedWorkers> {
            lastFilters = filters
            return AppResult.Success(
                PagedWorkers(items = emptyList(), pageNumber = 1, totalPages = 1, hasMore = false),
            )
        }
    }

    private class FakeTradeCatalogRepository : CatalogRepository {
        override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun searchBarrios(query: String) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getAllRubros() = AppResult.Success(listOf(CatalogOption(1, "Plomería")))
        override suspend fun getAllSubRubros() = AppResult.Success(emptyList<CatalogOption>())
    }
}
