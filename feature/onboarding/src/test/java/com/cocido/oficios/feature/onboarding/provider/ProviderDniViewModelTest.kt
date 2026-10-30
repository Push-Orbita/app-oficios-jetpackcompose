package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.ProviderSelfieDocument
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.model.WorkCertificateInfo
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.WorkCertificateRepository
import com.cocido.oficios.domain.usecase.profile.DeleteProviderIdentityDocumentUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDniDocumentsUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDocumentoUseCase
import com.cocido.oficios.domain.usecase.profile.GetWorkCertificatesUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UploadWorkCertificateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        val identity = FakeIdentityRepository()
        val viewModel = createViewModel(identity, savedDocumento = null)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImage("/tmp/front.jpg")
        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("upload:/tmp/front.jpg,/tmp/back.jpg"), identity.calls)
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `submit with incomplete form explains what is missing and does not call backend`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository()
        val viewModel = createViewModel(identity, savedDocumento = null)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        assertTrue(identity.calls.isEmpty())
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `picking a new photo replaces the previous local path`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeIdentityRepository())
        advanceUntilIdle()

        viewModel.onFrontImage("/tmp/front-blurry.jpg")
        viewModel.onFrontImage("/tmp/front-ok.jpg")

        val state = viewModel.uiState.value
        assertEquals("/tmp/front-ok.jpg", state.frontImagePath)
        assertEquals(DocumentSlotStatus.ReadyToSend, state.frontSlot.status)
        assertEquals("/tmp/front-ok.jpg", state.frontSlot.previewModel)
    }

    @Test
    fun `cancelling the picker keeps the photo already chosen`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeIdentityRepository())
        advanceUntilIdle()

        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.onBackImage(null)

        assertEquals("/tmp/back.jpg", viewModel.uiState.value.backImagePath)
    }

    @Test
    fun `dni in review stays editable and offers replacement`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeIdentityRepository(remoteDni = remoteDni("SUBIDO")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditable)
        assertEquals(DocumentSlotStatus.InReview, state.frontSlot.status)
        assertTrue(state.frontSlot.status.canReplace)
        assertEquals("https://cdn/front.jpg", state.frontSlot.previewModel)

        viewModel.onDocumentoChanged("87654321")
        assertEquals("87654321", viewModel.uiState.value.documento)
    }

    @Test
    fun `dni in review needs both sides before resubmitting`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository(remoteDni = remoteDni("SUBIDO"))
        val viewModel = createViewModel(identity)
        advanceUntilIdle()

        viewModel.onFrontImage("/tmp/front.jpg")
        assertTrue(viewModel.uiState.value.needsBothSidesToReplace)
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.submit()
        advanceUntilIdle()
        assertTrue(identity.calls.isEmpty())

        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("delete:31", "upload:/tmp/front.jpg,/tmp/back.jpg"), identity.calls)
    }

    @Test
    fun `rejected dni with existing document id uses replace for the changed side only`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository(remoteDni = remoteDni(front = "RECHAZADO", back = "APROBADO"))
        val viewModel = createViewModel(identity)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onFrontImage("/tmp/front-new.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("replace:31:/tmp/front-new.jpg,null"), identity.calls)
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
    }

    @Test
    fun `unchanged documentation continues without calling the backend`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository(remoteDni = remoteDni("SUBIDO"))
        val profile = FakeProfileRepository(documento = "12345678")
        val viewModel = createViewModel(identity, profile = profile)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertTrue(identity.calls.isEmpty())
        assertTrue(profile.updatedDocumentos.isEmpty())
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
    }

    @Test
    fun `submit error clears loading and keeps the form editable for retry`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository(
            uploadResult = AppResult.Error(AppError.Network(message = "No internet connection")),
        )
        val viewModel = createViewModel(identity, savedDocumento = null)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImage("/tmp/front.jpg")
        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEditable)
        assertNotNull(state.error)
        assertEquals("/tmp/front.jpg", state.frontImagePath)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `failed upload after deleting a dni in review retries as a fresh upload`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository(
            remoteDni = remoteDni("SUBIDO"),
            uploadResult = AppResult.Error(AppError.Network(message = "Network error")),
        )
        val viewModel = createViewModel(identity)
        advanceUntilIdle()

        viewModel.onFrontImage("/tmp/front.jpg")
        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.existingDocumentId)
        identity.uploadResult = AppResult.Success(Unit)
        identity.calls.clear()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("upload:/tmp/front.jpg,/tmp/back.jpg"), identity.calls)
    }

    @Test
    fun `hung request times out and leaves loading with an actionable error`() = runTest(dispatcher) {
        val identity = FakeIdentityRepository(hangOnUpload = true)
        val viewModel = createViewModel(identity, savedDocumento = null)
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImage("/tmp/front.jpg")
        viewModel.onBackImage("/tmp/back.jpg")
        viewModel.submit()
        advanceTimeBy(1_000)
        assertTrue(viewModel.uiState.value.isLoading)

        advanceTimeBy(DOCUMENT_SUBMIT_TIMEOUT_MS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(SUBMIT_TIMEOUT_MESSAGE, state.error)
    }

    @Test
    fun `approved dni cannot be replaced`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeIdentityRepository(remoteDni = remoteDni("APROBADO")))
        advanceUntilIdle()

        viewModel.onFrontImage("/tmp/front.jpg")

        val state = viewModel.uiState.value
        assertNull(state.frontImagePath)
        assertEquals(DocumentSlotStatus.Approved, state.frontSlot.status)
        assertTrue(state.isDocumentoLocked)
    }

    private fun kotlinx.coroutines.test.TestScope.collectEffects(
        viewModel: ProviderDniViewModel,
    ): MutableList<ProviderStepEffect> {
        val effects = mutableListOf<ProviderStepEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects.add(it) } }
        return effects
    }

    private fun remoteDni(status: String) = remoteDni(front = status, back = status)

    private fun remoteDni(front: String, back: String) = ProviderDniDocuments(
        id = 31L,
        frontUrl = "https://cdn/front.jpg",
        backUrl = "https://cdn/back.jpg",
        frontStatus = front,
        backStatus = back,
        frontFileName = "front.jpg",
        backFileName = "back.jpg",
    )

    private class FakeIdentityRepository(
        var remoteDni: ProviderDniDocuments? = null,
        var uploadResult: AppResult<Unit> = AppResult.Success(Unit),
        private val hangOnUpload: Boolean = false,
    ) : IdentityDocumentRepository {
        val calls = mutableListOf<String>()

        override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String): AppResult<Unit> {
            calls += "upload:$frontImagePath,$backImagePath"
            if (hangOnUpload) awaitCancellation()
            return uploadResult
        }
        override suspend fun uploadSelfie(imagePath: String) = AppResult.Success(Unit)
        override suspend fun replaceSelfie(documentId: Long, imagePath: String) = AppResult.Success(Unit)
        override suspend fun replaceDni(
            documentId: Long,
            frontImagePath: String?,
            backImagePath: String?,
        ): AppResult<Unit> {
            calls += "replace:$documentId:$frontImagePath,$backImagePath"
            return AppResult.Success(Unit)
        }
        override suspend fun deleteDocument(documentId: Long): AppResult<Unit> {
            calls += "delete:$documentId"
            remoteDni = null
            return AppResult.Success(Unit)
        }
        override suspend fun getMyDniDocuments(): AppResult<ProviderDniDocuments?> = AppResult.Success(remoteDni)
        override suspend fun getMySelfieDocument(): AppResult<ProviderSelfieDocument?> = AppResult.Success(null)
    }

    private class FakeProfileRepository(private val documento: String?) : ProviderProfileRepository {
        val updatedDocumentos = mutableListOf<String?>()
        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit> {
            updatedDocumentos += request.documento
            return AppResult.Success(Unit)
        }
        override suspend fun submitRubroFull(request: ProviderRubroFullRequest) = AppResult.Success(Unit)
        override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) = AppResult.Success(Unit)
        override suspend fun finalizeProfile() = AppResult.Success(Unit)
        override suspend fun getMySubRubros() = AppResult.Success(emptyList<ProviderSubRubroOption>())
        override suspend fun loadProviderProfileForSync() =
            AppResult.Success(ProviderProfileSync(tradeDraft = null, documento = documento))
    }

    private object FakeCertificates : WorkCertificateRepository {
        override suspend fun uploadCertificate(name: String, filePath: String): AppResult<WorkCertificateInfo> =
            AppResult.Error(AppError.Unknown())
        override suspend fun getMyCertificates(): AppResult<List<WorkCertificateInfo>> =
            AppResult.Success(emptyList())
    }

    private fun createViewModel(
        identity: FakeIdentityRepository,
        savedDocumento: String? = "12345678",
        profile: FakeProfileRepository = FakeProfileRepository(savedDocumento),
    ): ProviderDniViewModel = ProviderDniViewModel(
        updateProviderDniNumberUseCase = UpdateProviderDniNumberUseCase(profile),
        uploadProviderDniImagesUseCase = UploadProviderDniImagesUseCase(identity),
        replaceProviderDniImagesUseCase = ReplaceProviderDniImagesUseCase(identity),
        deleteProviderIdentityDocumentUseCase = DeleteProviderIdentityDocumentUseCase(identity),
        getProviderDniDocumentsUseCase = GetProviderDniDocumentsUseCase(identity),
        getProviderDocumentoUseCase = GetProviderDocumentoUseCase(profile),
        uploadWorkCertificateUseCase = UploadWorkCertificateUseCase(FakeCertificates),
        getWorkCertificatesUseCase = GetWorkCertificatesUseCase(FakeCertificates),
    )
}