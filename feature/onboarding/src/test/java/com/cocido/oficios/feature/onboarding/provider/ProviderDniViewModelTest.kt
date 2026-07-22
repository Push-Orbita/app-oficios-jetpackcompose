package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.usecase.profile.GetProviderDocumentoUseCase
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
    fun `submit sends number then images and emits saved effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImage("/tmp/front.jpg")
        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        job.cancel()
    }

    @Test
    fun `submit does nothing when form is incomplete`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onDocumentoChanged("12345678")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        job.cancel()
    }

    private fun identityRepository() = object : IdentityDocumentRepository {
        override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) =
            AppResult.Success(Unit)
        override suspend fun uploadSelfie(imagePath: String) = AppResult.Success(Unit)
        override suspend fun replaceSelfie(documentId: Long, imagePath: String) = AppResult.Success(Unit)
        override suspend fun getMyDniDocuments() = AppResult.Success(null)
        override suspend fun getMySelfieDocument() = AppResult.Success(null)
    }

    private fun createViewModel(): ProviderDniViewModel = ProviderDniViewModel(
        updateProviderDniNumberUseCase = UpdateProviderDniNumberUseCase(
            object : ProviderProfileRepository {
                override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
                    AppResult.Success(Unit)
                override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
                    AppResult.Success(Unit)
                override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) =
                    AppResult.Success(Unit)
                override suspend fun finalizeProfile() = AppResult.Success(Unit)
                override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
            },
        ),
        uploadProviderDniImagesUseCase = UploadProviderDniImagesUseCase(identityRepository()),
        getProviderDniDocumentsUseCase = com.cocido.oficios.domain.usecase.profile.GetProviderDniDocumentsUseCase(
            identityRepository(),
        ),
        getProviderDocumentoUseCase = GetProviderDocumentoUseCase(
            object : ProviderProfileRepository {
                override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
                    AppResult.Success(Unit)
                override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
                    AppResult.Success(Unit)
                override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) =
                    AppResult.Success(Unit)
                override suspend fun finalizeProfile() = AppResult.Success(Unit)
                override suspend fun loadProviderProfileForSync() =
                    AppResult.Success(ProviderProfileSync(tradeDraft = null, documento = "12345678"))
            },
        ),
        uploadWorkCertificateUseCase = com.cocido.oficios.domain.usecase.profile.UploadWorkCertificateUseCase(
            object : com.cocido.oficios.domain.repository.WorkCertificateRepository {
                override suspend fun uploadCertificate(name: String, filePath: String) =
                    AppResult.Error(com.cocido.oficios.core.common.result.AppError.Unknown())
                override suspend fun getMyCertificates(): AppResult<List<com.cocido.oficios.domain.model.WorkCertificateInfo>> =
                    AppResult.Success(emptyList())
            },
        ),
        getWorkCertificatesUseCase = com.cocido.oficios.domain.usecase.profile.GetWorkCertificatesUseCase(
            object : com.cocido.oficios.domain.repository.WorkCertificateRepository {
                override suspend fun uploadCertificate(name: String, filePath: String) =
                    AppResult.Error(com.cocido.oficios.core.common.result.AppError.Unknown())
                override suspend fun getMyCertificates(): AppResult<List<com.cocido.oficios.domain.model.WorkCertificateInfo>> =
                    AppResult.Success(emptyList())
            },
        ),
    )
}
