package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderAccountDetails
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfessionalProfileViewModelCacheTest {

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
    fun `previous profile screen sees gallery upload without being recreated`() = runTest(testDispatcher) {
        val accounts = SharedProfileAccountRepository()
        val useCases = AccountUseCases(accounts)
        val profileScreen = ProfessionalProfileViewModel(useCases)
        advanceUntilIdle()
        assertEquals(emptyList<Long>(), profileScreen.uiState.value.gallery.map { it.id })

        val portfolioScreen = PortfolioViewModel(
            savedStateHandle = androidx.lifecycle.SavedStateHandle(),
            accountUseCases = useCases,
        )
        advanceUntilIdle()
        portfolioScreen.onEvent(PortfolioEvent.GallerySelected("/tmp/nueva.jpg"))
        advanceUntilIdle()

        assertEquals(listOf(99L), profileScreen.uiState.value.gallery.map { it.id })
        assertEquals(listOf(99L), portfolioScreen.uiState.value.gallery.map { it.id })
    }
}

private class SharedProfileAccountRepository : AccountRepository {
    private val unused = AppResult.Error(AppError.Unknown("unused"))
    private val user = User(9, "t@test.com", "Emanuel", "Molina", UserRole.PROVIDER, true)
    private val _providerDetails = MutableStateFlow(
        ProviderAccountDetails(
            workerId = 9,
            status = "VALIDADO",
            document = "99887766",
            description = null,
            experience = null,
            paymentMethods = emptyList(),
            services = emptyList(),
            gallery = emptyList(),
            certificates = emptyList(),
        ),
    )
    private val _profile = MutableStateFlow(AccountProfile(user, _providerDetails.value))

    override val profile: StateFlow<AccountProfile?> = _profile.asStateFlow()
    override val providerDetails: StateFlow<ProviderAccountDetails?> = _providerDetails.asStateFlow()

    override suspend fun getProfile(): AppResult<AccountProfile> {
        val current = AccountProfile(user, _providerDetails.value)
        _profile.value = current
        return AppResult.Success(current)
    }

    override suspend fun getGallery() = AppResult.Success(_providerDetails.value.gallery)
    override suspend fun getCertificates() = AppResult.Success(_providerDetails.value.certificates)

    override suspend fun uploadGallery(filePaths: List<String>): AppResult<List<WorkGalleryItem>> {
        val added = listOf(WorkGalleryItem(99L, "https://cdn/99.jpg"))
        _providerDetails.value = _providerDetails.value.copy(gallery = _providerDetails.value.gallery + added)
        _profile.value = AccountProfile(user, _providerDetails.value)
        return AppResult.Success(added)
    }

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
    override suspend fun deleteGalleryItem(id: Long) = unused
    override suspend fun getCurrentDeletionRequest() = unused
    override suspend fun requestAccountDeletion(params: RequestAccountDeletionParams) = unused
    override suspend fun cancelAccountDeletion() = unused
}
