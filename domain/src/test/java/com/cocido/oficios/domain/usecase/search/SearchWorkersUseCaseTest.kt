package com.cocido.oficios.domain.usecase.search

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
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

    @Test
    fun `name matching a trade prefix still finds the worker by name`() = runTest {
        val repository = RecordingSearchRepository(
            byQuery = mapOf("plom" to listOf(worker(7, "Plomeyer Juan"))),
        )
        val useCase = SearchWorkersUseCase(repository, FakeTradeCatalogRepository())

        val result = useCase(WorkerSearchFilters(query = "plom"), pageNumber = 1)

        assertEquals(listOf(7L), (result as AppResult.Success).data.items.map { it.id })
        assertEquals("plom", repository.lastFilters?.query)
        assertNull(repository.lastFilters?.rubroId)
    }

    @Test
    fun `full name sends one word as q and keeps workers matching every word`() = runTest {
        val repository = RecordingSearchRepository(
            byQuery = mapOf(
                "Barberis" to listOf(
                    worker(1, "Facundo Barberis"),
                    worker(2, "Lucía Barberis"),
                ),
            ),
        )
        val useCase = SearchWorkersUseCase(repository, FakeTradeCatalogRepository())

        val result = useCase(WorkerSearchFilters(query = "facundo barberis"), pageNumber = 1)

        assertEquals(listOf(1L), (result as AppResult.Success).data.items.map { it.id })
        assertEquals(listOf("barberis"), repository.sentQueries.map { it?.lowercase() })
    }

    @Test
    fun `name inside a rubro keeps rubroId and q together`() = runTest {
        val repository = RecordingSearchRepository()
        val useCase = SearchWorkersUseCase(repository, FakeTradeCatalogRepository())

        useCase(WorkerSearchFilters(query = "Facundo", rubroId = 5), pageNumber = 1)

        assertEquals("Facundo", repository.lastFilters?.query)
        assertEquals(5L, repository.lastFilters?.rubroId)
        assertEquals(1, repository.sentQueries.size)
    }

    private fun worker(id: Long, name: String) = WorkerSummary(
        id = id,
        displayName = name,
        specialtyLabel = "",
        rating = null,
        avatarUrl = null,
        isVerified = true,
        primaryRubroId = null,
    )

    private class RecordingSearchRepository(
        private val byQuery: Map<String, List<WorkerSummary>> = emptyMap(),
    ) : WorkerSearchRepository {
        var lastFilters: WorkerSearchFilters? = null
        val sentQueries = mutableListOf<String?>()
        override suspend fun searchWorkers(
            filters: WorkerSearchFilters,
            pageNumber: Int,
            pageSize: Int,
        ): AppResult<PagedWorkers> {
            lastFilters = filters
            sentQueries += filters.query
            val items = byQuery.entries
                .firstOrNull { it.key.equals(filters.query, ignoreCase = true) }
                ?.value
                .orEmpty()
            return AppResult.Success(
                PagedWorkers(items = items, pageNumber = 1, totalPages = 1, hasMore = false),
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
