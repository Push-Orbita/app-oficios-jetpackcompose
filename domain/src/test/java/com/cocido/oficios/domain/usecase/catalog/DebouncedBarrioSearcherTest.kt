package com.cocido.oficios.domain.usecase.catalog

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.repository.CatalogRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebouncedBarrioSearcherTest {

    private class FakeCatalogRepository(
        private val gate: CompletableDeferred<Unit>? = null,
    ) : CatalogRepository {
        val queries = mutableListOf<String>()
        var results: List<CatalogOption> = listOf(
            CatalogOption(1, "Centro"),
            CatalogOption(2, "San Francisco"),
        )
        var error: AppError? = null

        override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> {
            queries += query
            gate?.await()
            error?.let { return AppResult.Error(it) }
            val filtered = if (query.isBlank()) {
                results
            } else {
                results.filter { it.label.contains(query, ignoreCase = true) }
            }
            return AppResult.Success(filtered)
        }
    }

    @Test
    fun `stable query triggers exactly one request after debounce`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeCatalogRepository()
        val results = mutableListOf<List<CatalogOption>>()
        val searcher = DebouncedBarrioSearcher(
            scope = this,
            searchBarrios = SearchBarriosUseCase(repo),
            onResult = { results += it },
            debounceMs = 300L,
        )

        searcher.submit("san")
        searcher.submit("san")
        searcher.submit("san")
        advanceTimeBy(299)
        assertTrue("debounce must hold the request, got ${repo.queries}", repo.queries.isEmpty())
        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(listOf("san"), repo.queries)
        assertEquals(1, results.size)
        assertEquals("San Francisco", results.single().single().label)
        searcher.cancel()
    }

    @Test
    fun `selecting does not search and resubmitting same query is ignored`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeCatalogRepository()
        val results = mutableListOf<List<CatalogOption>>()
        val searcher = DebouncedBarrioSearcher(
            scope = this,
            searchBarrios = SearchBarriosUseCase(repo),
            onResult = { results += it },
            debounceMs = 300L,
        )

        searcher.submit("san")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals("queries after first search: ${repo.queries}", 1, repo.queries.size)
        val resultsAfterSearch = results.size

        searcher.clearPending()
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals("select must not hit network", 1, repo.queries.size)
        assertEquals(resultsAfterSearch, results.size)

        searcher.submit("san")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(1, repo.queries.size)
        searcher.cancel()
    }

    @Test
    fun `rapid distinct queries keep only the latest request`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeCatalogRepository()
        val results = mutableListOf<List<CatalogOption>>()
        val searcher = DebouncedBarrioSearcher(
            scope = this,
            searchBarrios = SearchBarriosUseCase(repo),
            onResult = { results += it },
            debounceMs = 300L,
        )

        searcher.submit("c")
        advanceTimeBy(100)
        searcher.submit("ce")
        advanceTimeBy(100)
        searcher.submit("cen")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals(listOf("cen"), repo.queries)
        assertEquals(1, results.size)
        assertEquals("Centro", results.single().single().label)
        searcher.cancel()
    }

    @Test
    fun `clearPending drops in-flight result after select`() = runTest(UnconfinedTestDispatcher()) {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeCatalogRepository(gate)
        val results = mutableListOf<List<CatalogOption>>()
        val searcher = DebouncedBarrioSearcher(
            scope = this,
            searchBarrios = SearchBarriosUseCase(repo),
            onResult = { results += it },
            debounceMs = 300L,
        )

        searcher.submit("cen")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals("queries=${repo.queries}", listOf("cen"), repo.queries)
        assertTrue(results.isEmpty())

        searcher.clearPending()
        gate.complete(Unit)
        advanceUntilIdle()

        assertTrue(
            "in-flight result after clearPending must be dropped, got $results",
            results.isEmpty(),
        )
        searcher.cancel()
    }

    @Test
    fun `network error is reported instead of an empty list`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeCatalogRepository()
        repo.error = AppError.Network(message = "No internet connection")
        val results = mutableListOf<List<CatalogOption>>()
        val errors = mutableListOf<AppError>()
        val searcher = DebouncedBarrioSearcher(
            scope = this,
            searchBarrios = SearchBarriosUseCase(repo),
            onResult = { results += it },
            onError = { errors += it },
            debounceMs = 300L,
        )

        searcher.submit("Centro")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertEquals(listOf("Centro"), repo.queries)
        assertTrue("success callback must not run on error, got $results", results.isEmpty())
        assertEquals(1, errors.size)
        searcher.cancel()
    }

    @Test
    fun `retry repeats the same query after an error`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeCatalogRepository()
        repo.error = AppError.Network(message = "No internet connection")
        val results = mutableListOf<List<CatalogOption>>()
        val errors = mutableListOf<AppError>()
        val searcher = DebouncedBarrioSearcher(
            scope = this,
            searchBarrios = SearchBarriosUseCase(repo),
            onResult = { results += it },
            onError = { errors += it },
            debounceMs = 300L,
        )

        searcher.submit("Centro")
        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals(1, repo.queries.size)
        assertEquals(1, errors.size)

        repo.error = null
        searcher.retry()
        advanceUntilIdle()

        assertEquals(listOf("Centro", "Centro"), repo.queries)
        assertEquals(1, results.size)
        assertEquals("Centro", results.single().single().label)
        searcher.cancel()
    }
}
