package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderFinalizeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `success emits finalized effect`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(Unit))
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.finalize()
        advanceUntilIdle()

        assertTrue(effects.any { it is ProviderStepEffect.Finalized })
        assertFalse(viewModel.isLoading.value)
        job.cancel()
    }

    @Test
    fun `error stops loading without effect`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Error(AppError.Validation("Perfil incompleto")))
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.finalize()
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertFalse(viewModel.isLoading.value)
        job.cancel()
    }

    private fun createViewModel(result: AppResult<Unit>): ProviderFinalizeViewModel =
        ProviderFinalizeViewModel(
            FinalizeProviderProfileUseCase(
                object : ProviderProfileRepository {
                    override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
                        AppResult.Success(Unit)
                    override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
                        AppResult.Success(Unit)
                    override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) =
                        AppResult.Success(Unit)
                    override suspend fun finalizeProfile() = result
                    override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
                },
            ),
        )
}
