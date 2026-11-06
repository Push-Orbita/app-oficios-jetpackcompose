package com.cocido.oficios.feature.home.agenda.availability

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.usecase.GetMySubRubrosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvailabilityHubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeProviderProfileRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `returning to the screen refreshes without covering the list with a spinner`() = runTest(testDispatcher) {
        val viewModel = AvailabilityHubViewModel(GetMySubRubrosUseCase(repository))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.specialties.size)

        repository.result = AppResult.Success(listOf(manicura, pedicura))
        val seen = mutableListOf<AvailabilityHubUiState>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { seen += it }
        }
        viewModel.onResume() // coincide con el init
        viewModel.onResume()
        advanceUntilIdle()
        collector.cancel()

        assertTrue(seen.none { it.isLoading })
        assertEquals(listOf(1L, 2L), viewModel.uiState.value.specialties.map { it.id })
        assertEquals(2, repository.calls)
    }

    @Test
    fun `failed pull to refresh keeps the list and shows an inline error`() = runTest(testDispatcher) {
        val viewModel = AvailabilityHubViewModel(GetMySubRubrosUseCase(repository))
        advanceUntilIdle()

        repository.result = AppResult.Error(AppError.Network("Sin conexión"))
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.specialties.size)
        assertNull(state.errorMessage)
        assertEquals("Sin conexión", state.refreshErrorMessage)
        assertEquals(false, state.isRefreshing)
    }

    @Test
    fun `initial failure shows the error state`() = runTest(testDispatcher) {
        repository.result = AppResult.Error(AppError.Network("Sin conexión"))
        val viewModel = AvailabilityHubViewModel(GetMySubRubrosUseCase(repository))
        advanceUntilIdle()

        assertEquals("Sin conexión", viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    private val manicura = ProviderSubRubroOption(1, "Manicura", "Estética", requiresTurns = true)
    private val pedicura = ProviderSubRubroOption(2, "Pedicura", "Estética", requiresTurns = true)

    private inner class FakeProviderProfileRepository : ProviderProfileRepository {
        var result: AppResult<List<ProviderSubRubroOption>> = AppResult.Success(listOf(manicura))
        var calls = 0

        override suspend fun getMySubRubros(): AppResult<List<ProviderSubRubroOption>> {
            calls++
            return result
        }

        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) = AppResult.Success(Unit)
        override suspend fun submitRubroFull(request: ProviderRubroFullRequest) = AppResult.Success(Unit)
        override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) = AppResult.Success(Unit)
        override suspend fun finalizeProfile() = AppResult.Success(Unit)
        override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
    }
}
