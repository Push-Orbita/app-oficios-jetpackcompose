package com.cocido.oficios.feature.settings.client

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.LocationRepository
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeocodedPlace
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDataViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `new contact keeps CTA disabled and phone error under the phone field`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(ClientDataEvent.AddContact)

        assertFalse(viewModel.uiState.value.canSaveContact)
        assertNull(viewModel.uiState.value.contactPhoneError)
        assertNull(viewModel.uiState.value.contactEmailError)

        viewModel.onEvent(ClientDataEvent.ContactNameChanged("Daniel"))
        viewModel.onEvent(ClientDataEvent.ContactPhoneChanged("123"))

        assertEquals(
            "Ingrese el celular con el 9 de móvil, sin +54. Ejemplo: 9 370 5219109.",
            viewModel.uiState.value.contactPhoneError,
        )
        assertNull(viewModel.uiState.value.contactEmailError)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.canSaveContact)

        viewModel.onEvent(ClientDataEvent.ContactPhoneChanged("93705219109"))
        assertNull(viewModel.uiState.value.contactPhoneError)
        assertTrue(viewModel.uiState.value.canSaveContact)
    }

    @Test
    fun `save with missing phone does not blame the email field`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(ClientDataEvent.AddContact)
        viewModel.onEvent(ClientDataEvent.ContactNameChanged("Daniel"))
        viewModel.onEvent(ClientDataEvent.SaveContact)

        assertEquals("Ingrese su celular de WhatsApp.", viewModel.uiState.value.contactPhoneError)
        assertNull(viewModel.uiState.value.contactEmailError)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.canSaveContact)
    }

    @Test
    fun `typing barrio calle and altura moves the pin after debounce`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(ClientDataEvent.AddDomicilio)
        viewModel.onEvent(ClientDataEvent.BarrioSelected(CatalogOption(7, "San Miguel")))
        viewModel.onEvent(ClientDataEvent.StreetChanged("O'Higgins"))
        viewModel.onEvent(ClientDataEvent.NumberChanged("206"))
        advanceTimeBy(FormosaAddressGeocoding.LIVE_DEBOUNCE_MS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(-26.177, state.addressLatitude)
        assertEquals(-58.19, state.addressLongitude)
        assertTrue(state.canSaveDomicilio)
    }

    @Test
    fun `empty piso and dpto do not block saving a new domicilio`() = runTest(dispatcher) {
        val accounts = FakeClientDataAccountRepository()
        val viewModel = createViewModel(accounts)
        advanceUntilIdle()
        viewModel.onEvent(ClientDataEvent.AddDomicilio)
        viewModel.onEvent(ClientDataEvent.BarrioSelected(CatalogOption(7, "San Miguel")))
        viewModel.onEvent(ClientDataEvent.StreetChanged("O'Higgins"))
        viewModel.onEvent(ClientDataEvent.NumberChanged("206"))
        advanceTimeBy(FormosaAddressGeocoding.LIVE_DEBOUNCE_MS)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canSaveDomicilio)
        assertEquals("", viewModel.uiState.value.floor)
        assertEquals("", viewModel.uiState.value.apartment)

        viewModel.onEvent(ClientDataEvent.SaveDomicilio)
        advanceUntilIdle()

        val saved = requireNotNull(accounts.lastCreated)
        assertEquals("O'Higgins", saved.calle)
        assertEquals("206", saved.numero)
        assertEquals(null, saved.piso)
        assertEquals(null, saved.departamento)
        assertEquals(-26.177, saved.latitud)
        assertEquals(-58.19, saved.longitud)
    }

    @Test
    fun `delete contact emits success and updates the list without recreating`() = runTest(dispatcher) {
        val accounts = FakeClientDataAccountRepository(
            contacts = listOf(
                ClientContacto(11L, "Daniel", "93705219109", email = null, favorito = true),
            ),
        )
        val viewModel = createViewModel(accounts)
        advanceUntilIdle()
        val effects = mutableListOf<ClientDataEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onEvent(
            ClientDataEvent.RequestDelete(ClientDataDeleteTarget.Contact(11L, "el contacto")),
        )
        viewModel.onEvent(ClientDataEvent.ConfirmDelete)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contacts.none { it.id == 11L })
        assertTrue(effects.any { it is ClientDataEffect.ShowMessage && it.message.contains("contacto") })
        job.cancel()
    }

    private fun createViewModel(
        accounts: FakeClientDataAccountRepository = FakeClientDataAccountRepository(),
    ) = ClientDataViewModel(
        accountUseCases = AccountUseCases(accounts),
        searchBarriosUseCase = SearchBarriosUseCase(EmptyCatalog()),
        geocodeLocalAddress = GeocodeLocalAddressUseCase(FormosaGeocodingLocation()),
        getCurrentLocationUseCase = GetCurrentLocationUseCase(EmptyLocation()),
    )
}

private class FakeClientDataAccountRepository(
    contacts: List<ClientContacto> = emptyList(),
) : AccountRepository {
    private val unused = AppResult.Error(AppError.Unknown("unused"))
    private val user = User(1, "d@test.com", "Daniel", "Gonzales", UserRole.CLIENT, true, personaId = 2)
    private val _contactos = MutableStateFlow<List<ClientContacto>?>(contacts)

    override val contactos = _contactos.asStateFlow()

    override suspend fun getProfile() = AppResult.Success(AccountProfile(user))
    override suspend fun getClientContactos() = AppResult.Success(_contactos.value.orEmpty())
    override suspend fun getClientDomicilios() = AppResult.Success(emptyList<ClientDomicilio>())
    override suspend fun deleteClientContacto(id: Long): AppResult<Unit> {
        _contactos.value = _contactos.value.orEmpty().filterNot { it.id == id }
        return AppResult.Success(Unit)
    }
    override suspend fun createClientContacto(request: ClientContactoRequest) = AppResult.Success(21L)
    override suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest) =
        AppResult.Success(Unit)

    override suspend fun updateProfile(request: UpdateAccountProfileRequest) = unused
    override suspend fun changePassword(email: String, password: String, confirmation: String) = unused
    var lastCreated: ClientDomicilioRequest? = null
        private set

    override suspend fun createClientDomicilio(request: ClientDomicilioRequest): AppResult<Unit> {
        lastCreated = request
        return AppResult.Success(Unit)
    }
    override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest) = unused
    override suspend fun deleteClientDomicilio(id: Long) = unused
    override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) = unused
    override suspend fun setFavoriteContacto(id: Long, favorite: Boolean) = unused
    override suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest) = unused
    override suspend fun addProviderService(request: ProviderRubroFullRequest) = unused
    override suspend fun deleteProviderService(id: Long) = unused
    override suspend fun replaceProviderService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ) = unused
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

private class EmptyCatalog : CatalogRepository {
    override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun searchBarrios(query: String) = AppResult.Success(emptyList<CatalogOption>())
}

private class EmptyLocation : LocationRepository {
    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        AppResult.Error(AppError.Unknown("no"))
    override fun observeLocations(intervalMillis: Long) = flowOf<AppResult<GeoPoint>>()
}

private class FormosaGeocodingLocation : LocationRepository {
    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        AppResult.Error(AppError.Unknown("no"))
    override fun observeLocations(intervalMillis: Long) = flowOf<AppResult<GeoPoint>>()
    override suspend fun geocodeForward(query: String) = AppResult.Success(
        listOf(GeocodedPlace(-26.177, -58.19, query, locality = "Formosa", adminArea = "Formosa")),
    )
}
