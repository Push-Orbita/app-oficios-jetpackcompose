package com.cocido.oficios.feature.settings.provider

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ProviderCommercialLocal
import com.cocido.oficios.domain.model.ManagedProviderSubService
import com.cocido.oficios.domain.model.ManagedTimeRange
import com.cocido.oficios.domain.model.ManagedWorkSchedule
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
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.LocationRepository
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.catalog.GetCategoriesUseCase
import com.cocido.oficios.domain.usecase.catalog.GetRubrosByCategoryUseCase
import com.cocido.oficios.domain.usecase.catalog.GetSubRubrosByRubroUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.model.AgendaBlock
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.repository.AvailabilityRepository
import com.cocido.oficios.domain.usecase.AvailabilityUseCases
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
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
class ServicesViewModelTest {

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
    fun `edit service emits navigate to edit`() = runTest(testDispatcher) {
        val viewModel = ServicesViewModel(AccountUseCases(FakeAccountRepository()))
        advanceUntilIdle()
        val effects = mutableListOf<ServicesEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(ServicesEvent.EditService(sampleService()))
        advanceUntilIdle()
        job.cancel()
        assertTrue(effects.any { it is ServicesEffect.NavigateToEdit && it.serviceId == 11L })
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddServiceViewModelTest {

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
    fun `phase keeps form in edit when errorMessage is informative`() {
        val state = AddServiceUiState(
            isLoading = false,
            isEditing = true,
            selectedRubroId = 2L,
            errorMessage = "Arrastre el pin hasta la entrada y confirme. La calle escrita no se modifica.",
        )
        assertEquals(OficiosContentPhase.Content, state.phase())
        assertTrue(state.hasFormContent)
    }

    @Test
    fun `phase is error only when loadFailed without form content`() {
        val state = AddServiceUiState(
            isLoading = false,
            loadFailed = true,
            isEditing = true,
            errorMessage = "No encontramos ese servicio.",
        )
        assertEquals(OficiosContentPhase.Error, state.phase())
    }

    @Test
    fun `edit mode preloads specialties modality and hours`() = runTest(testDispatcher) {
        val accounts = FakeAccountRepository()
        val catalog = FakeCatalogRepository()
        val viewModel = addViewModel(accounts, catalog, serviceId = 11L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertTrue(state.isPrimaryService)
        assertEquals(11L, state.editingServiceId)
        assertEquals(2L, state.selectedRubroId)
        assertEquals(setOf(21L, 22L), state.selectedSubRubroIds)
        assertTrue(state.servesAtClientHome)
        assertFalse(state.servesAtLocal)
        assertEquals(setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"), state.workDays)
        assertEquals("09:00", state.timeSlots.first().from)
        assertEquals("Plomería", state.selectedRubroLabel)
        assertEquals(OficiosContentPhase.Content, state.phase())
    }

    @Test
    fun `locate on map in edit stays on form and does not set loadFailed`() = runTest(testDispatcher) {
        val viewModel = addViewModel(FakeAccountRepository(), FakeCatalogRepository(), serviceId = 11L)
        advanceUntilIdle()
        viewModel.onEvent(AddServiceEvent.Next)
        viewModel.onEvent(AddServiceEvent.LocateOnMap)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loadFailed)
        assertEquals(OficiosContentPhase.Content, state.phase())
        assertEquals(
            "Arrastre el pin hasta la entrada y confirme. La calle escrita no se modifica.",
            state.localLocation.message,
        )
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `edit prefills barrio street and number from commercial local`() = runTest(testDispatcher) {
        val accounts = FakeAccountRepository(localService = true)
        val viewModel = addViewModel(accounts, FakeCatalogRepository(), serviceId = 11L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.servesAtLocal)
        assertEquals(7L, state.localBarrioId)
        assertEquals("Centro", state.localBarrioQuery)
        assertEquals("Av. 25 de Mayo", state.localStreet)
        assertEquals("450", state.localNumber)
        assertEquals(OficiosContentPhase.Content, state.phase())
    }

    @Test
    fun `save in edit mode replaces via delete plus create`() = runTest(testDispatcher) {
        val accounts = FakeAccountRepository()
        val catalog = FakeCatalogRepository()
        val viewModel = addViewModel(accounts, catalog, serviceId = 11L)
        advanceUntilIdle()
        viewModel.onEvent(AddServiceEvent.Next)
        viewModel.onEvent(AddServiceEvent.Next)
        viewModel.onEvent(AddServiceEvent.Next)
        viewModel.onEvent(AddServiceEvent.PaymentToggled("EFECTIVO", true))
        viewModel.onEvent(AddServiceEvent.Next)
        val effects = mutableListOf<AddServiceEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(AddServiceEvent.Save)
        advanceUntilIdle()
        job.cancel()
        assertEquals(11L, accounts.replacedId)
        assertEquals(true, accounts.replacedPrimary)
        assertTrue(effects.any { it is AddServiceEffect.Saved && it.message == "Servicio actualizado." })
    }

    private fun addViewModel(
        accounts: FakeAccountRepository,
        catalog: FakeCatalogRepository,
        serviceId: Long?,
    ) = AddServiceViewModel(
        savedStateHandle = SavedStateHandle(buildMap { if (serviceId != null) put("serviceId", serviceId) }),
        accountUseCases = AccountUseCases(accounts),
        getCategories = GetCategoriesUseCase(catalog),
        getRubros = GetRubrosByCategoryUseCase(catalog),
        getSubRubros = GetSubRubrosByRubroUseCase(catalog),
        searchBarrios = SearchBarriosUseCase(catalog),
        geocodeLocalAddress = GeocodeLocalAddressUseCase(FakeLocationRepository()),
        availabilityUseCases = AvailabilityUseCases(FakeAvailabilityRepository()),
    )
}

private class FakeAvailabilityRepository : AvailabilityRepository {
    override suspend fun getWeeklyAvailability(subRubroId: Long) =
        AppResult.Success(WeeklyAvailability.default())
    override suspend fun replaceWeeklyAvailability(subRubroId: Long, week: WeeklyAvailability) =
        AppResult.Success(week)
    override suspend fun listBlocks(subRubroId: Long) = AppResult.Success(emptyList<AgendaBlock>())
    override suspend fun createBlock(params: CreateAgendaBlockParams) =
        AppResult.Success(AgendaBlock(id = 1, date = params.date))
    override suspend fun deleteBlock(subRubroId: Long, blockId: Long) = AppResult.Success(Unit)
}

private fun sampleService(atLocal: Boolean = false) = ManagedProviderService(
    id = 11L,
    rubroId = 2L,
    rubroName = "Plomería",
    isPrimary = true,
    subServices = listOf(
        ManagedProviderSubService(1, 21L, "Reparación de cañerías", true, atLocal, false),
        ManagedProviderSubService(2, 22L, "Instalación de sanitarios", true, atLocal, false),
    ),
    workSchedule = listOf(
        ManagedWorkSchedule(
            dayFrom = "LUNES",
            dayTo = "VIERNES",
            ranges = listOf(ManagedTimeRange("09:00", "18:00")),
        ),
    ),
)

private class FakeAccountRepository(
    private val localService: Boolean = false,
) : AccountRepository {
    var replacedId: Long? = null
    var replacedPrimary: Boolean? = null
    private val unused = AppResult.Error(AppError.Unknown("unused"))

    override suspend fun getProfile(): AppResult<AccountProfile> = AppResult.Success(
        AccountProfile(
            user = User(9, "t@test.com", "Emanuel", "Molina", UserRole.PROVIDER, true),
            providerDetails = ProviderAccountDetails(
                workerId = 9,
                status = "VALIDADO",
                document = "99887766",
                description = null,
                experience = null,
                paymentMethods = listOf("EFECTIVO"),
                services = listOf(sampleService(atLocal = localService)),
                gallery = emptyList(),
                certificates = emptyList(),
                commercialLocal = if (localService) {
                    ProviderCommercialLocal(
                        id = 4,
                        barrioId = 7,
                        barrioName = "Centro",
                        street = "Av. 25 de Mayo",
                        number = "450",
                        name = "Local",
                    )
                } else {
                    null
                },
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
        return AppResult.Success(Unit)
    }

    override suspend fun addProviderService(request: ProviderRubroFullRequest) = AppResult.Success(Unit)
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

private class FakeCatalogRepository : CatalogRepository {
    override suspend fun getCategories() = AppResult.Success(listOf(CatalogOption(1, "Construcción")))
    override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(listOf(CatalogOption(2, "Plomería")))
    override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(
        listOf(
            CatalogOption(21, "Reparación de cañerías"),
            CatalogOption(22, "Instalación de sanitarios"),
            CatalogOption(23, "Destape"),
        ),
    )
    override suspend fun searchBarrios(query: String) = AppResult.Success(
        listOf(CatalogOption(7, "Centro")).filter { it.label.contains(query, ignoreCase = true) || query.isBlank() },
    )
}

private class FakeLocationRepository : LocationRepository {
    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        AppResult.Error(AppError.Unknown("unused"))

    override fun observeLocations(intervalMillis: Long): Flow<AppResult<com.cocido.oficios.domain.model.GeoPoint>> =
        emptyFlow()
}
