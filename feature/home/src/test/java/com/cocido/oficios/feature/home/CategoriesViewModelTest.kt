package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.model.HomeCategoryItem
import com.cocido.oficios.domain.usecase.GetHomeCategoriesUseCase
import com.cocido.oficios.domain.usecase.TrackRecentCategoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

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
        val viewModel = CategoriesViewModel(
            getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
            trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CategoriesUiState.Success)
    }

    @Test
    fun `search submitted emits navigate effect`() = runTest(testDispatcher) {
        val viewModel = CategoriesViewModel(
            getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
            trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
        )
        advanceUntilIdle()

        val effects = mutableListOf<CategoriesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(CategoriesEvent.SearchSubmitted("electricista"))
        advanceUntilIdle()
        job.cancel()

        assertTrue(effects.any { it is CategoriesEffect.NavigateToSearchResults })
    }

    @Test
    fun `invalid category id is ignored`() = runTest(testDispatcher) {
        val viewModel = CategoriesViewModel(
            getHomeCategoriesUseCase = GetHomeCategoriesUseCase(FakeHomeRepository()),
            trackRecentCategoryUseCase = TrackRecentCategoryUseCase(FakeHomeRepository()),
        )
        advanceUntilIdle()

        viewModel.onEvent(
            CategoriesEvent.CategorySelected(HomeCategoryItem(id = -1L, name = "Ver todos")),
        )
        advanceUntilIdle()
    }
}
