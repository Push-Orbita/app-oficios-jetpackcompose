package com.cocido.oficios.feature.home.orders

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.CatalogOption
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
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
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
class MisUbicacionesViewModelTest {

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
    fun `loads domicilios and selects the favorite`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as MisUbicacionesUiState.Success
        assertEquals(2, state.domicilios.size)
        assertEquals(3L, state.selectedId)
        assertEquals(MisUbicacionesMode.PICKER, state.mode)
    }

    @Test
    fun `confirm emits the selected domicilio`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        val effects = mutableListOf<MisUbicacionesEffect>()
        val job = launch { viewModel.effects.collect { effects += it } }

        viewModel.onEvent(MisUbicacionesEvent.LocationSelected(8L))
        viewModel.onEvent(MisUbicacionesEvent.Confirm)
        advanceUntilIdle()
        job.cancel()

        assertEquals(listOf(MisUbicacionesEffect.Confirmed(8L)), effects)
    }

    @Test
    fun `add opens structured form instead of inventing free-text address`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(MisUbicacionesEvent.AddClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value as MisUbicacionesUiState.Success
        assertEquals(MisUbicacionesMode.ADD, state.mode)
        assertFalse(state.canSaveNew)
    }

    @Test
    fun `save without fields shows errors under barrio street and number`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onEvent(MisUbicacionesEvent.AddClicked)
        viewModel.onEvent(MisUbicacionesEvent.SaveNew)
        advanceUntilIdle()

        val state = viewModel.uiState.value as MisUbicacionesUiState.Success
        assertEquals("Seleccione un barrio de la lista.", state.addressBarrioError)
        assertEquals("Ingrese la calle.", state.addressStreetError)
        assertEquals("Ingrese la altura.", state.addressNumberError)
        assertFalse(state.canSaveNew)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `complete address enables save`() = runTest(testDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onEvent(MisUbicacionesEvent.AddClicked)
        viewModel.onEvent(MisUbicacionesEvent.BarrioSelected(CatalogOption(7, "Centro")))
        viewModel.onEvent(MisUbicacionesEvent.StreetChanged("San Martín"))
        viewModel.onEvent(MisUbicacionesEvent.NumberChanged("123"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as MisUbicacionesUiState.Success
        assertTrue(state.canSaveNew)
        assertEquals(null, state.addressBarrioError)
        assertEquals(null, state.addressStreetError)
        assertEquals(null, state.addressNumberError)
    }

    private fun viewModel() = MisUbicacionesViewModel(
        savedStateHandle = SavedStateHandle(mapOf("selectedId" to 3L)),
        accountUseCases = AccountUseCases(FakeLocationsAccountRepository()),
        searchBarriosUseCase = SearchBarriosUseCase(FakeLocationsCatalogRepository()),
        geocodeLocalAddress = GeocodeLocalAddressUseCase(FakeLocationsLocationRepository()),
    )
}

private fun sampleDomicilio(
    id: Long,
    street: String,
    number: String,
    favorite: Boolean,
) = ClientDomicilio(
    id = id,
    barrioId = 1L,
    barrioName = "San Juan",
    nombre = null,
    calle = street,
    numero = number,
    casaTorre = null,
    departamento = null,
    piso = null,
    favorito = favorite,
)

private class FakeLocationsAccountRepository : AccountRepository {
    private val unused = AppResult.Error(AppError.Unknown("unused"))
    private val savedDomicilios = mutableListOf(
        sampleDomicilio(3L, "Fuerza Aérea", "237", true),
        sampleDomicilio(8L, "Juan Luis Díaz", "1275", false),
    )

    override suspend fun getProfile() = AppResult.Success(
        AccountProfile(
            user = User(1, "c@test.com", "Nicolás", "Ayala", UserRole.CLIENT, true, personaId = 11L),
        ),
    )

    override suspend fun getClientDomicilios() = AppResult.Success(savedDomicilios.toList())
    override suspend fun createClientDomicilio(request: ClientDomicilioRequest): AppResult<Unit> {
        savedDomicilios += sampleDomicilio(90L, request.calle.orEmpty(), request.numero.orEmpty(), request.favorito)
        return AppResult.Success(Unit)
    }
    override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean): AppResult<Unit> {
        val index = savedDomicilios.indexOfFirst { it.id == id }
        if (index >= 0) savedDomicilios[index] = savedDomicilios[index].copy(favorito = favorite)
        return AppResult.Success(Unit)
    }

    override suspend fun updateProfile(request: UpdateAccountProfileRequest) = unused
    override suspend fun changePassword(email: String, password: String, confirmation: String) = unused
    override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest) = unused
    override suspend fun deleteClientDomicilio(id: Long) = unused
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

private class FakeLocationsCatalogRepository : CatalogRepository {
    override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
    override suspend fun searchBarrios(query: String) = AppResult.Success(listOf(CatalogOption(7, "Centro")))
}

private class FakeLocationsLocationRepository : LocationRepository {
    override suspend fun getCurrentLocation(requirePrecise: Boolean) =
        AppResult.Success(GeoPoint(-26.1849, -58.1731))
    override fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>> = emptyFlow()
}
