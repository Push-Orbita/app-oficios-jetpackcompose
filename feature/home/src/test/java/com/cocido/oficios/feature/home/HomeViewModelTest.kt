package com.cocido.oficios.feature.home

import com.cocido.oficios.domain.usecase.GetHomeContentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HomeViewModelTest {

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
    fun `load content emits success state`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(GetHomeContentUseCase(repository))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `simulate error emits error state`() = runTest(testDispatcher) {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(GetHomeContentUseCase(repository))

        advanceUntilIdle()
        viewModel.onEvent(HomeEvent.SimulateError)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }
}
