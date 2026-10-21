package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ManagedProviderSubService
import com.cocido.oficios.domain.model.ManagedTimeRange
import com.cocido.oficios.domain.model.ManagedWorkSchedule
import com.cocido.oficios.domain.model.ProviderAccountDetails
import com.cocido.oficios.domain.model.ProviderCommercialLocal
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
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.LocationRepository
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
class CommercialLocalViewModelTest {

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
    fun `prefills barrio and street from GET perfil domicilios`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.missingService)
        assertEquals("Centro", state.barrioQuery)
        assertEquals(7L, state.selectedBarrioId)
        assertEquals("San Martín 123", state.calle)
    }

    @Test
    fun `without published service shows empty state instead of saving`() = runTest(testDispatcher) {
        val accounts = FakeCommercialAccountRepository(services = emptyList())
        val viewModel = createViewModel(accounts)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.missingService)
        val effects = mutableListOf<CommercialLocalEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(CommercialLocalEvent.Submit)
        advanceUntilIdle()
        job.cancel()
        assertEquals(null, accounts.replacedId)
        assertTrue(effects.none { it is CommercialLocalEffect.ShowSuccess })
    }

    @Test
    fun `submit replaces primary service with lugarLocal and domicilioLocal`() = runTest(testDispatcher) {
        val accounts = FakeCommercialAccountRepository()
        val viewModel = createViewModel(accounts)
        advanceUntilIdle()
        viewModel.onEvent(CommercialLocalEvent.BarrioSelected(CatalogOption(7, "Centro")))
        viewModel.onEvent(CommercialLocalEvent.CalleChanged("Av. 25 de Mayo 100"))
        viewModel.onEvent(CommercialLocalEvent.MapLocationSelected(-26.1849, -58.1731))
        val effects = mutableListOf<CommercialLocalEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(CommercialLocalEvent.Submit)
        advanceUntilIdle()
        job.cancel()

        assertEquals(11L, accounts.replacedId)
        assertEquals(true, accounts.replacedPrimary)
        val request = requireNotNull(accounts.replacedRequest)
        assertTrue(request.servesAtLocal)
        assertEquals(7L, request.domicilioLocal?.barrioId)
        assertEquals("Av. 25 de Mayo 100", request.domicilioLocal?.calle)
        assertEquals(listOf(21L, 22L), request.subRubroIds)
        assertTrue(effects.any { it is CommercialLocalEffect.ShowSuccess })
        assertTrue(effects.any { it is CommercialLocalEffect.NavigateBack })
    }

    @Test
    fun `submit without map keeps form and does not call API`() = runTest(testDispatcher) {
        val accounts = FakeCommercialAccountRepository()
        val viewModel = createViewModel(accounts)
        advanceUntilIdle()
        viewModel.onEvent(CommercialLocalEvent.BarrioSelected(CatalogOption(7, "Centro")))
        viewModel.onEvent(CommercialLocalEvent.CalleChanged("Av. 25 de Mayo 100"))
        viewModel.onEvent(CommercialLocalEvent.Submit)
        advanceUntilIdle()
        assertEquals(null, accounts.replacedId)
        assertEquals("Complete barrio, calle y la ubicación en el mapa.", viewModel.uiState.value.errorMessage)
    }

    private fun createViewModel(
        accounts: FakeCommercialAccountRepository = FakeCommercialAccountRepository(),
    ) = CommercialLocalViewModel(
        accountUseCases = AccountUseCases(accounts),
        searchBarrios = SearchBarriosUseCase(FakeCommercialCatalogRepository()),
        getCurrentLocation = GetCurrentLocationUseCase(FakeCommercialLocationRepository()),
    )
}

private fun sampleService(
    servesAtLocal: Boolean = false,
) = ManagedProviderService(
    id = 11L,
    rubroId = 2L,
    rubroName = "Plomería",
    isPrimary = true,
    subServices = listOf(
        ManagedProviderSubService(1, 21L, "Reparación de cañerías", true, servesAtLocal, false),
        ManagedProviderSubService(2, 22L, "Instalación de sanitarios", true, servesAtLocal, false),
    ),
    workSchedule = listOf(
        ManagedWorkSchedule(
            dayFrom = "LUNES",
            dayTo = "VIERNES",
            ranges = listOf(ManagedTimeRange("09:00", "18:00")),
        ),
    ),
)

private class FakeCommercialAccountRepository(
    private val services: List<ManagedProviderService> = listOf(sampleService()),
) : AccountRepository {
    var replacedId: Long? = null
    var replacedPrimary: Boolean? = null
    var replacedRequest: ProviderRubroFullRequest? = null
    private val unused = AppResult.Error(AppError.Unknown("unused"))

    override suspend fun getProfile(): AppResult<AccountProfile> = AppResult.Success(
        AccountProfile(
            user = User(9, "t@test.com", "Matias", "Britez", UserRole.PROVIDER, true),
            providerDetails = ProviderAccountDetails(
                workerId = 9,
                status = "VALIDADO",
                document = "99887766",
                description = null,
                experience = null,
                paymentMethods = listOf("EFECTIVO"),
                services = services,
                gallery = emptyList(),
                certificates = emptyList(),
                commercialLocal = ProviderCommercialLocal(
                    barrioId = 7,
                    barrioName = "Centro",
                    street = "San Martín 123",
                ).takeIf { services.isNotEmpty() },
            ),
        ),
    )

    override suspend fun replaceProviderService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ): AppResult<Unit> {
        replacedId = existingId
        replacedPrimary = isPrimary
        replacedRequest = request
        return AppResult.Success(Unit)
    }

    override suspend fun addProviderService(request: ProviderRubroFullRequest) = unused
    override suspend fun deleteProviderService(id: Long) = unused
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
    override suspend fun getCertificates() = unused
    override suspend fun uploadCertificate(name: String, filePath: String) = unused
    override suspend fun updateCertificate(id: Long, name: String, currentUrl: String?, filePath: String?) = unused
    override suspend fun deleteCertificate(id: Long) = unused
    override suspend fun getGallery() = unused
    override suspend fun uploadGallery(filePaths: List<String>) = unused
    override suspend fun deleteGalleryItem(id: Long) = unused
    override suspend fun getCurrentDeletionRequest() = unused
    override suspend fun requestAccountDeletion(params: RequestAccountDeletionParams) = unused
    override suspend fun cancelAccountDeletion() = unused
}

private class FakeCommercialCatalogRepository : CatalogRepository {
    override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun searchBarrios(query: String) = AppResult.Success(listOf(CatalogOption(7, "Centro")))
}

private class FakeCommercialLocationRepository : LocationRepository {
    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        AppResult.Success(GeoPoint(-26.1849, -58.1731))

    override fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>> = emptyFlow()
}
