package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.feature.settings.navigation.SettingsDestination
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
class PortfolioViewModelTest {

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
    fun `gallery focus keeps the portfolio list`() = runTest(testDispatcher) {
        val viewModel = portfolioViewModel(SettingsDestination.FOCUS_GALLERY)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.certificateEditorVisible)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `certificate focus opens the add certificate editor`() = runTest(testDispatcher) {
        val viewModel = portfolioViewModel(SettingsDestination.FOCUS_CERTIFICATE)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.certificateEditorVisible)
        assertTrue(viewModel.uiState.value.editingCertificateId == null)
    }

    @Test
    fun `canSaveCertificate requires name and file on create`() {
        val empty = PortfolioUiState(isLoading = false)
        assertFalse(empty.canSaveCertificate)
        assertFalse(empty.copy(certificateName = "Matrícula").canSaveCertificate)
        assertTrue(
            empty.copy(certificateName = "Matrícula municipal", certificateFilePath = "/tmp/a.jpg")
                .canSaveCertificate,
        )
        assertTrue(empty.copy(certificateName = "Matrícula municipal", editingCertificateId = 9L).canSaveCertificate)
        assertFalse(
            empty.copy(
                certificateName = "Matrícula municipal",
                certificateFilePath = "/tmp/a.jpg",
                isSaving = true,
            ).canSaveCertificate,
        )
    }

    @Test
    fun `save certificate without file keeps editor and shows inline error`() = runTest(testDispatcher) {
        val viewModel = portfolioViewModel("")
        advanceUntilIdle()
        viewModel.onEvent(PortfolioEvent.AddCertificate)
        viewModel.onEvent(PortfolioEvent.CertificateNameChanged("Matrícula municipal"))
        viewModel.onEvent(PortfolioEvent.SaveCertificate)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.certificateEditorVisible)
        assertEquals("Seleccione el archivo del certificado.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.canSaveCertificate)
    }

    @Test
    fun `successful gallery delete removes the photo from ui`() = runTest(testDispatcher) {
        val accounts = FakePortfolioAccountRepository(
            gallery = listOf(
                WorkGalleryItem(12L, "https://cdn.example/12.jpg"),
                WorkGalleryItem(13L, "https://cdn.example/13.jpg"),
            ),
        )
        val viewModel = PortfolioViewModel(
            savedStateHandle = SavedStateHandle(),
            accountUseCases = AccountUseCases(accounts),
        )
        advanceUntilIdle()
        viewModel.onEvent(PortfolioEvent.RequestDelete(PortfolioDeleteTarget.Gallery(12L)))
        viewModel.onEvent(PortfolioEvent.ConfirmDelete)
        advanceUntilIdle()
        assertEquals(listOf(13L), viewModel.uiState.value.gallery.map { it.id })
        assertEquals(listOf(12L), accounts.deletedGalleryIds)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    private fun portfolioViewModel(focus: String) = PortfolioViewModel(
        savedStateHandle = SavedStateHandle(mapOf("focus" to focus)),
        accountUseCases = AccountUseCases(FakePortfolioAccountRepository()),
    )
}

private class FakePortfolioAccountRepository(
    gallery: List<WorkGalleryItem> = emptyList(),
) : AccountRepository {
    private val unused = AppResult.Error(AppError.Unknown("unused"))
    private val galleryItems = gallery.toMutableList()
    val deletedGalleryIds = mutableListOf<Long>()

    override suspend fun getCertificates() = AppResult.Success(emptyList<WorkCertificateItem>())
    override suspend fun getGallery() = AppResult.Success(galleryItems.toList())

    override suspend fun getProfile(): AppResult<AccountProfile> = unused
    override suspend fun updateProfile(request: UpdateAccountProfileRequest) = unused
    override suspend fun changePassword(email: String, password: String, confirmation: String) = unused
    override suspend fun getClientDomicilios() = unused
    override suspend fun createClientDomicilio(request: ClientDomicilioRequest) = unused
    override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest) = unused
    override suspend fun deleteClientDomicilio(id: Long) = unused
    override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) = unused
    override suspend fun getClientContactos() = unused
    override suspend fun createClientContacto(request: ClientContactoRequest) = unused
    override suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest) = unused
    override suspend fun deleteClientContacto(id: Long) = unused
    override suspend fun setFavoriteContacto(id: Long, favorite: Boolean) = unused
    override suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest) = unused
    override suspend fun addProviderService(request: ProviderRubroFullRequest) = unused
    override suspend fun deleteProviderService(id: Long) = unused
    override suspend fun replaceProviderService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ) = unused
    override suspend fun uploadCertificate(name: String, filePath: String) = unused
    override suspend fun updateCertificate(id: Long, name: String, currentUrl: String?, filePath: String?) = unused
    override suspend fun deleteCertificate(id: Long) = unused
    override suspend fun uploadGallery(filePaths: List<String>) = unused
    override suspend fun deleteGalleryItem(id: Long): AppResult<Unit> {
        deletedGalleryIds += id
        galleryItems.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
    override suspend fun getCurrentDeletionRequest() = unused
    override suspend fun requestAccountDeletion(params: RequestAccountDeletionParams) = unused
    override suspend fun cancelAccountDeletion() = unused
}
