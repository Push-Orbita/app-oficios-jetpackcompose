package com.cocido.oficios.feature.onboarding.client

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientContactoViewModelTest {

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
    fun `preload fills name and email from account`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("Juan Pérez", viewModel.uiState.value.nombre)
        assertEquals("juan@test.com", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.telefono)
    }

    @Test
    fun `empty submit shows error`() {
        val viewModel = createViewModel()
        viewModel.submit()
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `empty form cannot submit`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `valid form emits saved effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<ClientContactoEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.onNombreChanged("Juan")
        viewModel.onTelefonoChanged("1122334455")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.any { it is ClientContactoEffect.Saved })
        job.cancel()
    }

    private fun createViewModel(): ClientContactoViewModel = ClientContactoViewModel(
        saveClientContactoUseCase = SaveClientContactoUseCase(
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
        getCurrentUserUseCase = GetCurrentUserUseCase(
            object : AuthRepository {
                private val user = User(1, "juan@test.com", "Juan", "Pérez", UserRole.CLIENT, true)
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
                override suspend fun resetPassword(
                    resetToken: String,
                    password: String,
                    confirmPassword: String,
                ) = AppResult.Success(Unit)
                override suspend fun sendVerificationEmail() = AppResult.Success("ok")
                override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
                override suspend fun getUserRoles() = AppResult.Success(
                    listOf(UserRoleDefinition(1, "Cliente", UserRole.CLIENT)),
                )
            },
        ),
    )
}
