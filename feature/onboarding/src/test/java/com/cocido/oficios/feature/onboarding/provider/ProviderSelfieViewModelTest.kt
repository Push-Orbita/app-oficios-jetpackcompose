package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderSelfieDocument
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.usecase.profile.DeleteProviderIdentityDocumentUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderSelfieUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
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
class ProviderSelfieViewModelTest {

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
    fun `new selfie is uploaded once on submit`() = runTest(dispatcher) {
        val identity = FakeIdentity()
        val viewModel = createViewModel(identity)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onImageSelected("/tmp/selfie-1.jpg")
        viewModel.onImageSelected("/tmp/selfie-2.jpg")
        assertTrue(identity.calls.isEmpty())

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("upload:/tmp/selfie-2.jpg"), identity.calls)
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
    }

    @Test
    fun `cancelling the picker keeps the selfie already chosen`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeIdentity())
        advanceUntilIdle()

        viewModel.onImageSelected("/tmp/selfie.jpg")
        viewModel.onImageSelected(null)

        assertEquals("/tmp/selfie.jpg", viewModel.uiState.value.imagePath)
    }

    @Test
    fun `selfie in review can continue without changes`() = runTest(dispatcher) {
        val identity = FakeIdentity(remote = remoteSelfie("SUBIDO"))
        val viewModel = createViewModel(identity)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canSubmit)
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(identity.calls.isEmpty())
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
    }

    @Test
    fun `selfie in review is replaced by delete plus upload`() = runTest(dispatcher) {
        val identity = FakeIdentity(remote = remoteSelfie("SUBIDO"))
        val viewModel = createViewModel(identity)
        advanceUntilIdle()

        viewModel.onImageSelected("/tmp/selfie-new.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("delete:32", "upload:/tmp/selfie-new.jpg"), identity.calls)
    }

    @Test
    fun `rejected selfie with existing document id uses replace`() = runTest(dispatcher) {
        val identity = FakeIdentity(remote = remoteSelfie("RECHAZADO"))
        val viewModel = createViewModel(identity)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
        viewModel.onImageSelected("/tmp/selfie-new.jpg")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(listOf("replace:32:/tmp/selfie-new.jpg"), identity.calls)
    }

    @Test
    fun `submit error clears loading and keeps the selfie for retry`() = runTest(dispatcher) {
        val identity = FakeIdentity(uploadResult = AppResult.Error(AppError.Network(message = "Network error")))
        val viewModel = createViewModel(identity)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        viewModel.onImageSelected("/tmp/selfie.jpg")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("/tmp/selfie.jpg", state.imagePath)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `approved selfie cannot be replaced`() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeIdentity(remote = remoteSelfie("APROBADO")))
        advanceUntilIdle()

        viewModel.onImageSelected("/tmp/selfie.jpg")

        assertNull(viewModel.uiState.value.imagePath)
        assertEquals(DocumentSlotStatus.Approved, viewModel.uiState.value.slot.status)
    }

    private fun TestScope.collectEffects(viewModel: ProviderSelfieViewModel): MutableList<ProviderStepEffect> {
        val effects = mutableListOf<ProviderStepEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.collect { effects.add(it) } }
        return effects
    }

    private fun remoteSelfie(status: String) = ProviderSelfieDocument(
        id = 32L,
        url = "https://cdn/selfie.jpg",
        status = status,
        rejectionReason = null,
    )

    private class FakeIdentity(
        var remote: ProviderSelfieDocument? = null,
        val uploadResult: AppResult<Unit> = AppResult.Success(Unit),
    ) : IdentityDocumentRepository {
        val calls = mutableListOf<String>()
        override suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) = AppResult.Success(Unit)
        override suspend fun uploadSelfie(imagePath: String): AppResult<Unit> {
            calls += "upload:$imagePath"
            return uploadResult
        }
        override suspend fun replaceSelfie(documentId: Long, imagePath: String): AppResult<Unit> {
            calls += "replace:$documentId:$imagePath"
            return AppResult.Success(Unit)
        }
        override suspend fun replaceDni(documentId: Long, frontImagePath: String?, backImagePath: String?) =
            AppResult.Success(Unit)
        override suspend fun deleteDocument(documentId: Long): AppResult<Unit> {
            calls += "delete:$documentId"
            remote = null
            return AppResult.Success(Unit)
        }
        override suspend fun getMyDniDocuments(): AppResult<ProviderDniDocuments?> = AppResult.Success(null)
        override suspend fun getMySelfieDocument(): AppResult<ProviderSelfieDocument?> = AppResult.Success(remote)
    }

    private fun createViewModel(identity: FakeIdentity) = ProviderSelfieViewModel(
        uploadProviderSelfieUseCase = UploadProviderSelfieUseCase(identity),
        replaceProviderSelfieUseCase = ReplaceProviderSelfieUseCase(identity),
        deleteProviderIdentityDocumentUseCase = DeleteProviderIdentityDocumentUseCase(identity),
        getProviderSelfieUseCase = GetProviderSelfieUseCase(identity),
    )
}