package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
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
class ProviderDniViewModelTest {

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
    fun `submit number emits saved effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onDocumentoChanged("12345678")
        viewModel.submitNumber()
        advanceUntilIdle()

        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        job.cancel()
    }

    @Test
    fun `submit images emits saved effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onFrontImage("/tmp/front.jpg")
        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submitImages()
        advanceUntilIdle()

        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        job.cancel()
    }

    private fun createViewModel(): ProviderDniViewModel = ProviderDniViewModel(
        updateProviderDniNumberUseCase = UpdateProviderDniNumberUseCase(
            object : ProviderProfileRepository {
                override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
                    AppResult.Success(Unit)
                override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
                    AppResult.Success(Unit)
                override suspend fun finalizeProfile() = AppResult.Success(Unit)
            },
        ),
        uploadProviderDniImagesUseCase = UploadProviderDniImagesUseCase(
            object : IdentityDocumentRepository {
                override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) =
                    AppResult.Success(Unit)
            },
        ),
    )
}
