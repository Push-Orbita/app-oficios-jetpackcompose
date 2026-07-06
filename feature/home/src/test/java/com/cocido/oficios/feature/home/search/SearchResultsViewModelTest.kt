package com.cocido.oficios.feature.home.search

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters
import com.cocido.oficios.domain.model.WorkerSummary
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.WorkerSearchRepository
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.search.SearchWorkersUseCase
import com.cocido.oficios.domain.usecase.search.TrackRecentSearchUseCase
import com.cocido.oficios.feature.home.FakeRecentSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchResultsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load emits success state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Success)
    }

    @Test
    fun `query submit refreshes results`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SearchResultsEvent.QueryChanged("plomero"))
        viewModel.onEvent(SearchResultsEvent.QuerySubmitted)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is SearchResultsUiState.Success)
        assertEquals("plomero", viewModel.searchQuery.value)
    }

    @Test
    fun `error emits error state`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            searchResult = AppResult.Error(AppError.Unknown()),
        )
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchResultsUiState.Error)
    }

    private fun createViewModel(
        searchResult: AppResult<PagedWorkers> = AppResult.Success(
            PagedWorkers(
                items = listOf(
                    WorkerSummary(
                        id = 1L,
                        displayName = "Test Worker",
                        specialtyLabel = "Plomería",
                        rating = 4.5f,
                        avatarUrl = null,
                        isVerified = true,
                        primaryRubroId = 3L,
                    ),
                ),
                pageNumber = 1,
                totalPages = 1,
                hasMore = false,
            ),
        ),
    ): SearchResultsViewModel {
        val repository = object : WorkerSearchRepository {
            override suspend fun searchWorkers(
                filters: WorkerSearchFilters,
                pageNumber: Int,
                pageSize: Int,
            ) = searchResult
        }
        val catalogRepository = object : CatalogRepository {
            override suspend fun getCategories(): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
            override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> =
                AppResult.Success(emptyList())
        }
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "rubroId" to 3L,
                "rubroLabel" to "Plomería",
            ),
        )
        return SearchResultsViewModel(
            savedStateHandle = savedStateHandle,
            searchWorkersUseCase = SearchWorkersUseCase(repository),
            trackRecentSearchUseCase = TrackRecentSearchUseCase(FakeRecentSearchRepository()),
            getCategoriesUseCase = GetCategoriesUseCase(catalogRepository),
            getRubrosByCategoryUseCase = GetRubrosByCategoryUseCase(catalogRepository),
            getSubRubrosByRubroUseCase = GetSubRubrosByRubroUseCase(catalogRepository),
            searchBarriosUseCase = SearchBarriosUseCase(catalogRepository),
        )
    }
}
