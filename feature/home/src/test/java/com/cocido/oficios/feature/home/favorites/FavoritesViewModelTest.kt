package com.cocido.oficios.feature.home.favorites

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.model.FavoriteWorker
import com.cocido.oficios.domain.repository.FavoritesRepository
import com.cocido.oficios.domain.usecase.social.GetFavoritesUseCase
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
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(
                FakeFavoritesRepository(AppResult.Success(FavoritePage(emptyList(), 1, false))),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertTrue(state.errorMessage == null)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `hire clicked emits NavigateToHire`() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(
                FakeFavoritesRepository(
                    AppResult.Success(
                        FavoritePage(
                            items = listOf(
                                FavoriteWorker(1, 9, "Matias", "Plomería", 4.8f, null, true),
                            ),
                            pageNumber = 1,
                            hasMore = false,
                        ),
                    ),
                ),
            ),
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
        val viewModel = FavoritesViewModel(
            getFavoritesUseCase = GetFavoritesUseCase(
                FakeFavoritesRepository(AppResult.Error(com.cocido.oficios.core.common.result.AppError.Network("network error"))),
            ),
        )
        advanceUntilIdle()

        assertEquals(
            "No pudimos conectarnos. Revisá tu conexión e intentá nuevamente.",
            viewModel.uiState.value.errorMessage,
        )
    }
}

private class FakeFavoritesRepository(
    private val pageResult: AppResult<FavoritePage>,
) : FavoritesRepository {
    override suspend fun getFavorites(pageNumber: Int, pageSize: Int) = pageResult
    override suspend fun getStatus(workerId: Long) = error("unused")
    override suspend fun add(workerId: Long, idempotencyKey: String) = error("unused")
    override suspend fun remove(workerId: Long) = error("unused")
}
