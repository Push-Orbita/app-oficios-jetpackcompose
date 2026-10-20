package com.cocido.oficios.feature.onboarding.client

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
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
import com.cocido.oficios.domain.usecase.location.GetCurrentLocationUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientDomicilioViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val user = User(1, "u@test.com", "Juan", "Pérez", UserRole.CLIENT, true, personaId = 50)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invalid form does not submit`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.submit()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `success emits saved effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ClientDomicilioEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onBarrioQueryChanged("Ce")
        advanceTimeBy(300)
        advanceUntilIdle()
        viewModel.onBarrioSelected(CatalogOption(id = 1, label = "Centro"))
        viewModel.onCalleChanged("San Martín")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.any { it is ClientDomicilioEffect.Saved })
        job.cancel()
    }

    private fun createViewModel(): ClientDomicilioViewModel = ClientDomicilioViewModel(
        saveClientDomicilioUseCase = SaveClientDomicilioUseCase(
            object : ClientProfileRepository {
                override suspend fun saveDomicilio(request: ClientDomicilioRequest) = AppResult.Success(Unit)
                override suspend fun saveContacto(request: ClientContactoRequest) = AppResult.Success(1L)
                override suspend fun sendPhoneVerificationCode(contactId: Long) =
                    AppResult.Success(com.cocido.oficios.domain.model.PhoneVerificationSendResult("ok", null))
                override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
                override suspend fun getMyDomicilios() = AppResult.Success(emptyList<com.cocido.oficios.domain.model.ClientDomicilio>())
                override suspend fun getMyContactos() = AppResult.Success(emptyList<com.cocido.oficios.domain.model.ClientContacto>())
            },
        ),
        getCurrentUserUseCase = GetCurrentUserUseCase(FakeAuthRepository(user)),
        searchBarriosUseCase = SearchBarriosUseCase(
            object : CatalogRepository {
                override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
                override suspend fun getRubrosByCategory(categoryId: Long) =
                    AppResult.Success(emptyList<CatalogOption>())
                override suspend fun getSubRubrosByRubro(rubroId: Long) =
                    AppResult.Success(emptyList<CatalogOption>())
                override suspend fun searchBarrios(query: String) =
                    AppResult.Success(listOf(CatalogOption(1, "Centro")))
            },
        ),
        getCurrentLocationUseCase = GetCurrentLocationUseCase(
            object : LocationRepository {
                override suspend fun getCurrentLocation(requirePrecise: Boolean) =
                    AppResult.Success(com.cocido.oficios.domain.model.GeoPoint(-26.1849, -58.1731))

                override fun observeLocations(intervalMillis: Long) =
                    kotlinx.coroutines.flow.flowOf(
                        AppResult.Success(com.cocido.oficios.domain.model.GeoPoint(-26.1849, -58.1731)),
                    )
            },
        ),
    )

    private class FakeAuthRepository(
        private val user: User,
    ) : AuthRepository {
        override suspend fun login(email: String, password: String) = AppResult.Success(
            UserSession(AuthTokens("a", "r"), user),
        )
        override suspend fun registerClient(request: RegisterClientRequest) = AppResult.Success(
            UserSession(AuthTokens("a", "r"), user),
        )
        override suspend fun registerProvider(request: RegisterProviderRequest) = AppResult.Success(
            UserSession(AuthTokens("a", "r"), user),
        )
        override suspend fun refreshToken(refreshToken: String) = AppResult.Success(AuthTokens("a", "r"))
        override suspend fun getCurrentUser() = AppResult.Success(user)
        override suspend fun logout() = AppResult.Success(Unit)
        override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
        override suspend fun verifyPasswordResetCode(code: String) = AppResult.Success("token")
        override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
            AppResult.Success(Unit)
        override suspend fun sendVerificationEmail() = AppResult.Success("Código de verificación reenviado a tu correo")
        override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
        override suspend fun getUserRoles() = AppResult.Success(
            listOf(UserRoleDefinition(1, "Cliente", UserRole.CLIENT)),
        )
    }
}
