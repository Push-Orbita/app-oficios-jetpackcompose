package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.ProviderSelfieDocument
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.WorkCertificateRepository
import com.cocido.oficios.domain.usecase.profile.GetProviderDniDocumentsUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDocumentoUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.GetWorkCertificatesUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UploadWorkCertificateUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderIdentityViewModelTest {

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
    fun `canSubmit es false cuando faltan datos obligatorios`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit es false cuando solo tiene DNI front`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit es false cuando solo tiene DNI front y back pero falta selfie`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit es true cuando tiene todos los obligatorios`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.canSubmit)
    }

    @Test
    fun `canSubmit es true con documentos remotos cargados`() = runTest(dispatcher) {
        val viewModel = createViewModelWithRemoteDocuments()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.canSubmit)
        assertTrue(state.frontRemoteLoaded)
        assertTrue(state.backRemoteLoaded)
        assertTrue(state.selfieRemoteLoaded)
    }

    @Test
    fun `submit muestra error cuando falta documento`() = runTest(dispatcher) {
        val viewModel = createViewModel(documento = null)
        advanceUntilIdle()

        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Completá el número de DNI y todas las fotos obligatorias", state.uploadError)
        assertFalse(state.canSubmit)
    }

    @Test
    fun `submit exitoso emite efecto Saved`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(null, state.uploadError)
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        job.cancel()
    }

    @Test
    fun `submit con certificado opcional lo sube si tiene nombre y path`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ProviderStepEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")
        viewModel.onCertificateNameChanged("Curso de Electricidad")
        viewModel.onCertificateSelected("/path/to/cert.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        job.cancel()
    }

    private fun identityRepository() = object : IdentityDocumentRepository {
        override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) =
            AppResult.Success(Unit)
        override suspend fun uploadSelfie(imagePath: String) = AppResult.Success(Unit)
        override suspend fun replaceSelfie(documentId: Long, imagePath: String) = AppResult.Success(Unit)
        override suspend fun getMyDniDocuments() = AppResult.Success<ProviderDniDocuments?>(null)
        override suspend fun getMySelfieDocument() = AppResult.Success<ProviderSelfieDocument?>(null)
    }

    private fun identityRepositoryWithRemoteDocuments() = object : IdentityDocumentRepository {
        override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) =
            AppResult.Success(Unit)
        override suspend fun uploadSelfie(imagePath: String) = AppResult.Success(Unit)
        override suspend fun replaceSelfie(documentId: Long, imagePath: String) = AppResult.Success(Unit)
        override suspend fun getMyDniDocuments() = AppResult.Success(
            ProviderDniDocuments(
                frontUrl = "https://example.com/front.jpg",
                backUrl = "https://example.com/back.jpg",
                frontFileName = "front.jpg",
                backFileName = "back.jpg",
                frontStatus = "APPROVED",
                backStatus = "APPROVED",
            ),
        )
        override suspend fun getMySelfieDocument() = AppResult.Success(
            ProviderSelfieDocument(
                id = 1L,
                url = "https://example.com/selfie.jpg",
                rejectionReason = null,
                status = "APPROVED",
            ),
        )
    }

    private fun profileRepository(documento: String? = "12345678") = object : ProviderProfileRepository {
        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
            AppResult.Success(Unit)
        override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
            AppResult.Success(Unit)
        override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) =
            AppResult.Success(Unit)
        override suspend fun finalizeProfile() = AppResult.Success(Unit)
        override suspend fun getMySubRubros() = AppResult.Success(emptyList<com.cocido.oficios.domain.model.ProviderSubRubroOption>())
        override suspend fun loadProviderProfileForSync() =
            AppResult.Success(ProviderProfileSync(tradeDraft = null, documento = documento))
    }

    private fun workCertificateRepository() = object : WorkCertificateRepository {
        override suspend fun uploadCertificate(name: String, filePath: String) =
            AppResult.Success(com.cocido.oficios.domain.model.WorkCertificateInfo(id = 1L, name = name, fileUrl = "https://example.com/cert.jpg"))
        override suspend fun getMyCertificates() =
            AppResult.Success(emptyList<com.cocido.oficios.domain.model.WorkCertificateInfo>())
    }

    private fun createViewModel(documento: String? = "12345678"): ProviderIdentityViewModel = ProviderIdentityViewModel(
        updateProviderDniNumberUseCase = UpdateProviderDniNumberUseCase(profileRepository(documento)),
        uploadProviderDniImagesUseCase = UploadProviderDniImagesUseCase(identityRepository()),
        getProviderDniDocumentsUseCase = GetProviderDniDocumentsUseCase(identityRepository()),
        getProviderDocumentoUseCase = GetProviderDocumentoUseCase(profileRepository(documento)),
        uploadProviderSelfieUseCase = UploadProviderSelfieUseCase(identityRepository()),
        replaceProviderSelfieUseCase = ReplaceProviderSelfieUseCase(identityRepository()),
        getProviderSelfieUseCase = GetProviderSelfieUseCase(identityRepository()),
        uploadWorkCertificateUseCase = UploadWorkCertificateUseCase(workCertificateRepository()),
        getWorkCertificatesUseCase = GetWorkCertificatesUseCase(workCertificateRepository()),
    )

    private fun createViewModelWithRemoteDocuments(): ProviderIdentityViewModel = ProviderIdentityViewModel(
        updateProviderDniNumberUseCase = UpdateProviderDniNumberUseCase(profileRepository()),
        uploadProviderDniImagesUseCase = UploadProviderDniImagesUseCase(identityRepositoryWithRemoteDocuments()),
        getProviderDniDocumentsUseCase = GetProviderDniDocumentsUseCase(identityRepositoryWithRemoteDocuments()),
        getProviderDocumentoUseCase = GetProviderDocumentoUseCase(profileRepository()),
        uploadProviderSelfieUseCase = UploadProviderSelfieUseCase(identityRepositoryWithRemoteDocuments()),
        replaceProviderSelfieUseCase = ReplaceProviderSelfieUseCase(identityRepositoryWithRemoteDocuments()),
        getProviderSelfieUseCase = GetProviderSelfieUseCase(identityRepositoryWithRemoteDocuments()),
        uploadWorkCertificateUseCase = UploadWorkCertificateUseCase(workCertificateRepository()),
        getWorkCertificatesUseCase = GetWorkCertificatesUseCase(workCertificateRepository()),
    )
}
