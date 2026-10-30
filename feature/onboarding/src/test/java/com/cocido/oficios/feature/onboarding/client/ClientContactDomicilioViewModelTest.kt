package com.cocido.oficios.feature.onboarding.client

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.GeocodedPlace
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.model.PhoneVerificationSendResult
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.repository.LocationRepository
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.location.GeocodeLocalAddressUseCase
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.profile.GetClientContactosUseCase
import com.cocido.oficios.domain.usecase.profile.GetClientDomiciliosUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientContactDomicilioViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val user = User(1, "ana@test.com", "Ana", "Gómez", UserRole.CLIENT, true, personaId = 7)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `preload fills name and email from account when there is no contacto`() = runTest(dispatcher) {
        val viewModel = createViewModel(contactos = emptyList())
        advanceUntilIdle()

        assertEquals("Ana Gómez", viewModel.uiState.value.nombre)
        assertEquals("ana@test.com", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.telefono)
    }

    @Test
    fun `preload keeps saved contacto and fills blank email from account`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            contactos = listOf(
                ClientContacto(
                    id = 3,
                    nombre = "Ana del barrio",
                    telefonoWhatsapp = "3705219109",
                    email = " ",
                    favorito = true,
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals("Ana del barrio", viewModel.uiState.value.nombre)
        assertEquals("ana@test.com", viewModel.uiState.value.email)
        assertEquals("93705219109", viewModel.uiState.value.telefono)
    }

    @Test
    fun `empty piso and dpto do not block continuar`() = runTest(dispatcher) {
        val viewModel = createViewModel(emptyList())
        advanceUntilIdle()
        viewModel.onNombreChanged("Ana Gómez")
        viewModel.onTelefonoChanged("93705219109")
        viewModel.onBarrioSelected(CatalogOption(1, "Centro"))
        viewModel.onCalleChanged("O'Higgins")
        viewModel.onNumeroChanged("206")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canSubmit)
        assertEquals("", viewModel.uiState.value.piso)
        assertEquals("", viewModel.uiState.value.departamento)
    }

    @Test
    fun `submit saves domicilio before phone verification so a failed whatsapp send does not lose it`() =
        runTest(dispatcher) {
            val profile = RecordingProfile(initialContactos = emptyList(), newContactId = 15L)
            val viewModel = createViewModel(profile)
            val effects = mutableListOf<ClientContactDomicilioEffect>()
            val job = launch { viewModel.effects.collect { effects += it } }
            advanceUntilIdle()

            fillAndConfirm(viewModel)
            advanceUntilIdle()

            assertEquals(1, profile.createdDomicilios)
            assertEquals(1, profile.createdContactos)
            assertEquals(0, profile.phoneCodesSent)
            assertEquals(
                listOf(ClientContactDomicilioEffect.NavigateToPhoneVerification(15L, "93704123456")),
                effects,
            )
            job.cancel()
        }

    @Test
    fun `retrying guardar reuses contacto and domicilio instead of duplicating`() = runTest(dispatcher) {
        val profile = RecordingProfile(initialContactos = emptyList(), newContactId = 15L)
        val viewModel = createViewModel(profile)
        val job = launch { viewModel.effects.collect { } }
        advanceUntilIdle()

        fillAndConfirm(viewModel)
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, profile.createdContactos)
        assertEquals(1, profile.createdDomicilios)
        assertEquals(listOf(1L), profile.updatedDomicilios)
        job.cancel()
    }

    @Test
    fun `verified contacto skips phone verification and completes step`() = runTest(dispatcher) {
        val profile = RecordingProfile(
            initialContactos = listOf(
                ClientContacto(
                    id = 3,
                    nombre = "Ana Gómez",
                    telefonoWhatsapp = "+5493704123456",
                    telefonoVerificado = true,
                    email = "ana@test.com",
                    favorito = true,
                ),
            ),
            newContactId = 99L,
        )
        val viewModel = createViewModel(profile)
        val effects = mutableListOf<ClientContactDomicilioEffect>()
        val job = launch { viewModel.effects.collect { effects += it } }
        advanceUntilIdle()

        fillAndConfirm(viewModel)
        advanceUntilIdle()

        assertEquals(0, profile.createdContactos)
        assertEquals(1, profile.createdDomicilios)
        assertEquals(listOf<ClientContactDomicilioEffect>(ClientContactDomicilioEffect.Saved), effects)
        job.cancel()
    }

    private fun fillAndConfirm(viewModel: ClientContactDomicilioViewModel) {
        viewModel.onNombreChanged("Ana Gómez")
        viewModel.onTelefonoChanged("93704123456")
        viewModel.onEmailChanged("ana@test.com")
        viewModel.onBarrioSelected(CatalogOption(1, "Centro"))
        viewModel.onCalleChanged("O'Higgins")
        viewModel.onNumeroChanged("206")
        viewModel.onMapLocationSelected(-26.1849, -58.1731)
        viewModel.confirmPin()
    }

    private class RecordingProfile(
        initialContactos: List<ClientContacto>,
        private val newContactId: Long,
    ) : ClientProfileRepository {
        private val storedContactos = initialContactos.toMutableList()
        private val storedDomicilios = mutableListOf<ClientDomicilio>()
        var createdContactos = 0
        var createdDomicilios = 0
        val updatedDomicilios = mutableListOf<Long>()
        var phoneCodesSent = 0

        override suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit> {
            createdDomicilios++
            storedDomicilios += ClientDomicilio(
                id = storedDomicilios.size + 1L,
                barrioId = request.barrioId,
                barrioName = "Centro",
                nombre = request.nombre,
                calle = request.calle,
                numero = request.numero,
                casaTorre = null,
                departamento = request.departamento,
                piso = request.piso,
                favorito = true,
            )
            return AppResult.Success(Unit)
        }
        override suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long> {
            createdContactos++
            storedContactos += ClientContacto(
                id = newContactId,
                nombre = request.nombre,
                telefonoWhatsapp = "+54" + request.telefonoWhatsapp,
                email = request.email,
                favorito = true,
            )
            return AppResult.Success(newContactId)
        }
        override suspend fun updateDomicilio(id: Long, request: ClientDomicilioRequest): AppResult<Unit> {
            updatedDomicilios += id
            return AppResult.Success(Unit)
        }
        override suspend fun updateContacto(id: Long, request: ClientContactoRequest) = AppResult.Success(Unit)
        override suspend fun sendPhoneVerificationCode(contactId: Long): AppResult<PhoneVerificationSendResult> {
            phoneCodesSent++
            return AppResult.Success(PhoneVerificationSendResult("ok", null))
        }
        override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
        override suspend fun getMyDomicilios() = AppResult.Success(storedDomicilios.toList())
        override suspend fun getMyContactos() = AppResult.Success(storedContactos.toList())
    }

    private fun createViewModel(contactos: List<ClientContacto>): ClientContactDomicilioViewModel {
        val profile = object : ClientProfileRepository {
            override suspend fun saveDomicilio(request: ClientDomicilioRequest) = AppResult.Success(Unit)
            override suspend fun saveContacto(request: ClientContactoRequest) = AppResult.Success(1L)
            override suspend fun updateDomicilio(id: Long, request: ClientDomicilioRequest) = AppResult.Success(Unit)
            override suspend fun updateContacto(id: Long, request: ClientContactoRequest) = AppResult.Success(Unit)
            override suspend fun sendPhoneVerificationCode(contactId: Long) =
                AppResult.Success(PhoneVerificationSendResult("ok", null))
            override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
            override suspend fun getMyDomicilios() = AppResult.Success(emptyList<ClientDomicilio>())
            override suspend fun getMyContactos() = AppResult.Success(contactos)
        }
        return createViewModel(profile)
    }

    private fun createViewModel(profile: ClientProfileRepository): ClientContactDomicilioViewModel {
        return ClientContactDomicilioViewModel(
            saveClientContactoUseCase = SaveClientContactoUseCase(profile),
            saveClientDomicilioUseCase = SaveClientDomicilioUseCase(profile),
            getClientContactosUseCase = GetClientContactosUseCase(profile),
            getClientDomiciliosUseCase = GetClientDomiciliosUseCase(profile),
            getCurrentUserUseCase = GetCurrentUserUseCase(FakeAuthRepository(user)),
            searchBarriosUseCase = SearchBarriosUseCase(
                object : CatalogRepository {
                    override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
                    override suspend fun getRubrosByCategory(categoryId: Long) =
                        AppResult.Success(emptyList<CatalogOption>())
                    override suspend fun getSubRubrosByRubro(rubroId: Long) =
                        AppResult.Success(emptyList<CatalogOption>())
                    override suspend fun searchBarrios(query: String) =
                        AppResult.Success(emptyList<CatalogOption>())
                },
            ),
            geocodeLocalAddressUseCase = GeocodeLocalAddressUseCase(location),
            pendingClientOnboardingStore = PendingClientOnboardingStore(),
            getCurrentLocationUseCase = GetCurrentLocationUseCase(location),
        )
    }

    private val location = object : LocationRepository {
        override suspend fun getCurrentLocation(requirePrecise: Boolean) =
            AppResult.Success(GeoPoint(-26.1849, -58.1731))
        override fun observeLocations(intervalMillis: Long) =
            flowOf(AppResult.Success(GeoPoint(-26.1849, -58.1731)))
        override suspend fun geocodeForward(query: String) = AppResult.Success(
            listOf(GeocodedPlace(-26.1849, -58.1731, query)),
        )
    }

    private class FakeAuthRepository(private val user: User) : AuthRepository {
        override suspend fun login(email: String, password: String) =
            AppResult.Success(UserSession(AuthTokens("a", "r"), user))
        override suspend fun registerClient(request: RegisterClientRequest) =
            AppResult.Success(UserSession(AuthTokens("a", "r"), user))
        override suspend fun registerProvider(request: RegisterProviderRequest) =
            AppResult.Success(UserSession(AuthTokens("a", "r"), user))
        override suspend fun refreshToken(refreshToken: String) = AppResult.Success(AuthTokens("a", "r"))
        override suspend fun getCurrentUser() = AppResult.Success(user)
        override suspend fun logout() = AppResult.Success(Unit)
        override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
        override suspend fun verifyPasswordResetCode(code: String) = AppResult.Success("token")
        override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
            AppResult.Success(Unit)
        override suspend fun sendVerificationEmail() = AppResult.Success("ok")
        override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
        override suspend fun getUserRoles() = AppResult.Success(
            listOf(UserRoleDefinition(1, "Cliente", UserRole.CLIENT)),
        )
    }
}
