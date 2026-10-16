package com.cocido.oficios.feature.home.favorites

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.model.FavoriteWorker
import com.cocido.oficios.domain.repository.FavoritesRepository
import com.cocido.oficios.domain.usecase.social.GetFavoritesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class FavoritesViewModelTest {

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
    fun `empty page shows empty list without error`() = runTest(testDispatcher) {
        val repository = FakeFavoritesRepository(AppResult.Success(FavoritePage(emptyList(), 1, false)))
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(repository),
            favoritesRepository = repository,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertTrue(state.errorMessage == null)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `hire clicked emits NavigateToHire`() = runTest(testDispatcher) {
        val repository = FakeFavoritesRepository(
            AppResult.Success(
                FavoritePage(
                    items = listOf(
                        FavoriteWorker(1, 9, "Matias", "Plomería", 4.8f, null, true),
                    ),
                    pageNumber = 1,
                    hasMore = false,
                ),
            ),
        )
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(repository),
            favoritesRepository = repository,
        )
        advanceUntilIdle()

        val effects = mutableListOf<FavoritesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(FavoritesEvent.HireClicked(9))
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.any { it is FavoritesEffect.NavigateToHire && it.workerId == 9L })
    }

    @Test
    fun `error without items keeps message`() = runTest(testDispatcher) {
        val repository = FakeFavoritesRepository(
            AppResult.Error(com.cocido.oficios.core.common.result.AppError.Network("network error")),
        )
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(repository),
            favoritesRepository = repository,
        )
        advanceUntilIdle()

        assertEquals(
            "No pudimos conectarnos. Revisá tu conexión e intentá nuevamente.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun `reloads page one when repository emits a change without full screen spinner`() = runTest(testDispatcher) {
        val first = FavoriteWorker(1, 3, "Ana", "Plomería", null, null, true)
        val second = FavoriteWorker(2, 5, "Luis", "Gas", null, null, true)
        val repository = FakeFavoritesRepository(
            AppResult.Success(FavoritePage(listOf(first), 1, false)),
        )
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(repository),
            favoritesRepository = repository,
        )
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.items.size)

        repository.pageResult = AppResult.Success(FavoritePage(listOf(first, second), 1, false))
        repository.emitChange()
        advanceUntilIdle()

        assertEquals(listOf(3L, 5L), viewModel.uiState.value.items.map { it.workerId })
        assertTrue(!viewModel.uiState.value.isLoading)
        assertEquals(2, repository.loads)
    }

    @Test
    fun `first resume does not load twice`() = runTest(testDispatcher) {
        val repository = FakeFavoritesRepository(AppResult.Success(FavoritePage(emptyList(), 1, false)))
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(repository),
            favoritesRepository = repository,
        )
        advanceUntilIdle()
        viewModel.onResume()
        advanceUntilIdle()
        assertEquals(1, repository.loads)
    }
}

private class FakeFavoritesRepository(
    var pageResult: AppResult<FavoritePage>,
) : FavoritesRepository {
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val favoritesChanged: SharedFlow<Unit> = changes.asSharedFlow()
    var loads: Int = 0

    override suspend fun getFavorites(pageNumber: Int, pageSize: Int): AppResult<FavoritePage> {
        loads += 1
        return pageResult
    }

    override suspend fun getStatus(workerId: Long) = error("unused")
    override suspend fun add(workerId: Long, idempotencyKey: String) = error("unused")
    override suspend fun remove(workerId: Long) = error("unused")

    fun emitChange() {
        changes.tryEmit(Unit)
    }
}
