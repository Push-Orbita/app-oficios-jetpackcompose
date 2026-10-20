package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderSelfieDocument
import com.cocido.oficios.domain.model.WorkCertificate
import com.cocido.oficios.domain.usecase.profile.GetProviderDniDocumentsUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderDocumentoUseCase
import com.cocido.oficios.domain.usecase.profile.GetProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.GetWorkCertificatesUseCase
import com.cocido.oficios.domain.usecase.profile.ReplaceProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderDniNumberUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderDniImagesUseCase
import com.cocido.oficios.domain.usecase.profile.UploadProviderSelfieUseCase
import com.cocido.oficios.domain.usecase.profile.UploadWorkCertificateUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var viewModel: ProviderIdentityViewModel
    private lateinit var updateProviderDniNumberUseCase: UpdateProviderDniNumberUseCase
    private lateinit var uploadProviderDniImagesUseCase: UploadProviderDniImagesUseCase
    private lateinit var getProviderDniDocumentsUseCase: GetProviderDniDocumentsUseCase
    private lateinit var getProviderDocumentoUseCase: GetProviderDocumentoUseCase
    private lateinit var uploadProviderSelfieUseCase: UploadProviderSelfieUseCase
    private lateinit var replaceProviderSelfieUseCase: ReplaceProviderSelfieUseCase
    private lateinit var getProviderSelfieUseCase: GetProviderSelfieUseCase
    private lateinit var uploadWorkCertificateUseCase: UploadWorkCertificateUseCase
    private lateinit var getWorkCertificatesUseCase: GetWorkCertificatesUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        updateProviderDniNumberUseCase = mockk()
        uploadProviderDniImagesUseCase = mockk()
        getProviderDniDocumentsUseCase = mockk()
        getProviderDocumentoUseCase = mockk()
        uploadProviderSelfieUseCase = mockk()
        replaceProviderSelfieUseCase = mockk()
        getProviderSelfieUseCase = mockk()
        uploadWorkCertificateUseCase = mockk()
        getWorkCertificatesUseCase = mockk()

        every { getProviderDocumentoUseCase() } returns null
        coEvery { getProviderDniDocumentsUseCase() } returns AppResult.Success(null)
        coEvery { getProviderSelfieUseCase() } returns AppResult.Success(null)
        coEvery { getWorkCertificatesUseCase() } returns AppResult.Success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `canSubmit es false cuando faltan datos obligatorios`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit es false cuando solo tiene DNI front`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit es false cuando solo tiene DNI front y back pero falta selfie`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.canSubmit)
    }

    @Test
    fun `canSubmit es true cuando tiene todos los obligatorios`() = runTest {
        viewModel = createViewModel()
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
    fun `canSubmit es true con documentos remotos cargados`() = runTest {
        every { getProviderDocumentoUseCase() } returns "12345678"
        coEvery { getProviderDniDocumentsUseCase() } returns AppResult.Success(
            ProviderDniDocuments(
                frontUrl = "https://example.com/front.jpg",
                backUrl = "https://example.com/back.jpg",
                frontFileName = "front.jpg",
                backFileName = "back.jpg",
            ),
        )
        coEvery { getProviderSelfieUseCase() } returns AppResult.Success(
            ProviderSelfieDocument(
                id = 1L,
                url = "https://example.com/selfie.jpg",
                rejectionReason = null,
            ),
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.canSubmit)
        assertTrue(state.frontRemoteLoaded)
        assertTrue(state.backRemoteLoaded)
        assertTrue(state.selfieRemoteLoaded)
    }

    @Test
    fun `submit muestra error cuando falta documento`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Completá el número de DNI y todas las fotos obligatorias", state.uploadError)
    }

    @Test
    fun `submit error en DNI number detiene el flujo`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")

        coEvery { updateProviderDniNumberUseCase("12345678") } returns AppResult.Error.NetworkError

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.uploadError != null)
        coVerify(exactly = 1) { updateProviderDniNumberUseCase("12345678") }
        coVerify(exactly = 0) { uploadProviderDniImagesUseCase(any(), any()) }
    }

    @Test
    fun `submit error en DNI images detiene el flujo`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")

        coEvery { updateProviderDniNumberUseCase("12345678") } returns AppResult.Success(Unit)
        coEvery { uploadProviderDniImagesUseCase(any(), any()) } returns AppResult.Error.NetworkError

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.uploadError != null)
        coVerify(exactly = 1) { uploadProviderDniImagesUseCase(any(), any()) }
        coVerify(exactly = 0) { uploadProviderSelfieUseCase(any()) }
    }

    @Test
    fun `submit exitoso llama todos los use cases en orden`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")

        coEvery { updateProviderDniNumberUseCase("12345678") } returns AppResult.Success(Unit)
        coEvery { uploadProviderDniImagesUseCase("/path/to/front.jpg", "/path/to/back.jpg") } returns AppResult.Success(Unit)
        coEvery { uploadProviderSelfieUseCase("/path/to/selfie.jpg") } returns AppResult.Success(Unit)

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(null, state.uploadError)
        coVerify(exactly = 1) { updateProviderDniNumberUseCase("12345678") }
        coVerify(exactly = 1) { uploadProviderDniImagesUseCase("/path/to/front.jpg", "/path/to/back.jpg") }
        coVerify(exactly = 1) { uploadProviderSelfieUseCase("/path/to/selfie.jpg") }
    }

    @Test
    fun `submit con certificado opcional lo sube si tiene nombre y path`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDocumentoChanged("12345678")
        viewModel.onFrontImageSelected("/path/to/front.jpg")
        viewModel.onBackImageSelected("/path/to/back.jpg")
        viewModel.onSelfieImageSelected("/path/to/selfie.jpg")
        viewModel.onCertificateNameChanged("Curso de Electricidad")
        viewModel.onCertificateSelected("/path/to/cert.jpg")

        coEvery { updateProviderDniNumberUseCase(any()) } returns AppResult.Success(Unit)
        coEvery { uploadProviderDniImagesUseCase(any(), any()) } returns AppResult.Success(Unit)
        coEvery { uploadProviderSelfieUseCase(any()) } returns AppResult.Success(Unit)
        coEvery { uploadWorkCertificateUseCase("Curso de Electricidad", "/path/to/cert.jpg") } returns AppResult.Success(Unit)

        viewModel.submit()
        advanceUntilIdle()

        coVerify(exactly = 1) { uploadWorkCertificateUseCase("Curso de Electricidad", "/path/to/cert.jpg") }
    }

    private fun createViewModel() = ProviderIdentityViewModel(
        updateProviderDniNumberUseCase = updateProviderDniNumberUseCase,
        uploadProviderDniImagesUseCase = uploadProviderDniImagesUseCase,
        getProviderDniDocumentsUseCase = getProviderDniDocumentsUseCase,
        getProviderDocumentoUseCase = getProviderDocumentoUseCase,
        uploadProviderSelfieUseCase = uploadProviderSelfieUseCase,
        replaceProviderSelfieUseCase = replaceProviderSelfieUseCase,
        getProviderSelfieUseCase = getProviderSelfieUseCase,
        uploadWorkCertificateUseCase = uploadWorkCertificateUseCase,
        getWorkCertificatesUseCase = getWorkCertificatesUseCase,
    )
}
