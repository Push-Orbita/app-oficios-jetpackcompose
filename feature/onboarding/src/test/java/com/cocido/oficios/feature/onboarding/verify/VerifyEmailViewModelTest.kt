package com.cocido.oficios.feature.onboarding.verify

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.VerificationCodeCopy
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AccountRepository
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.auth.SendVerificationEmailUseCase
import com.cocido.oficios.domain.usecase.auth.VerifyEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
class VerifyEmailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val user = User(1, "user@test.com", "Juan", "Pérez", UserRole.CLIENT, false)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads email without sending code on init`() = runTest(dispatcher) {
        var sendCount = 0
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(
                user = user,
                onSendVerificationEmail = { sendCount++ },
            ),
        )

        advanceUntilIdle()

        assertEquals("user@test.com", viewModel.uiState.value.email)
        assertEquals(0, sendCount)
        assertNull(viewModel.uiState.value.infoMessage)
    }

    @Test
    fun `saving corrected email updates profile and resends code`() = runTest(dispatcher) {
        var sendCount = 0
        var updatedEmail: String? = null
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(
                user = user,
                onSendVerificationEmail = { sendCount++ },
            ),
            accountRepository = FakeAccountRepository { request ->
                updatedEmail = request.email
                AppResult.Success(
                    com.cocido.oficios.domain.model.AccountProfile(
                        user = user.copy(email = request.email),
                    ),
                )
            },
        )
        advanceUntilIdle()

        viewModel.onEditEmailClick()
        viewModel.onDraftEmailChanged("nuevo@test.com")
        viewModel.saveEmail()
        advanceUntilIdle()

        assertEquals("nuevo@test.com", updatedEmail)
        assertEquals("nuevo@test.com", viewModel.uiState.value.email)
        assertFalse(viewModel.uiState.value.isEditingEmail)
        assertEquals(1, sendCount)
    }

    @Test
    fun `invalid code cannot submit`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCodeChanged("123")
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `valid code emits verified effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<VerifyEmailEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        advanceUntilIdle()

        viewModel.onCodeChanged("123456")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(effects.any { it is VerifyEmailEffect.Verified })
        job.cancel()
    }

    @Test
    fun `submit va01 maps to code field not nest banner`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(
                user = user,
                verifyEmailResult = AppResult.Error(
                    AppError.Validation(
                        message = "Datos de entrada no válidos.",
                        backendCode = "VA01",
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onCodeChanged("874987")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(VerificationCodeCopy.EMAIL, state.codeError)
        assertNull(state.error)
        assertTrue(state.hasAttemptedSubmit)
        assertFalse(state.isLoading)
    }

    @Test
    fun `submit fieldErrors codigo shows backend spanish copy on the code`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(
                user = user,
                verifyEmailResult = AppResult.Error(
                    AppError.Validation(
                        message = "Datos de entrada no válidos.",
                        fieldErrors = mapOf("codigo" to "El código debe tener 6 dígitos"),
                        backendCode = "VA01",
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onCodeChanged("874987")
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("El código debe tener 6 dígitos", state.codeError)
        assertNull(state.error)
    }

    @Test
    fun `submit shows validation error for empty code`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Ingrese el código", viewModel.uiState.value.codeError)
        assertTrue(viewModel.uiState.value.hasAttemptedSubmit)
    }

    @Test
    fun `resend code is blocked during cooldown`() = runTest(dispatcher) {
        var sendCount = 0
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(
                user = user,
                onSendVerificationEmail = { sendCount++ },
            ),
        )
        advanceUntilIdle()

        viewModel.resendCode()
        runCurrent()
        while (viewModel.uiState.value.isResending) runCurrent()
        runCurrent()

        assertEquals(1, sendCount)
        assertTrue(viewModel.uiState.value.resendCooldownSeconds > 0)

        viewModel.resendCode()
        runCurrent()
        assertEquals(1, sendCount)

        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(0, viewModel.uiState.value.resendCooldownSeconds)

        viewModel.resendCode()
        advanceUntilIdle()
        assertEquals(2, sendCount)
    }

    private fun createViewModel(
        authRepository: FakeAuthRepository = FakeAuthRepository(user),
        accountRepository: FakeAccountRepository = FakeAccountRepository(),
    ): VerifyEmailViewModel = VerifyEmailViewModel(
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
        sendVerificationEmailUseCase = SendVerificationEmailUseCase(authRepository),
        verifyEmailUseCase = VerifyEmailUseCase(authRepository, FakeSessionRepository()),
        accountUseCases = com.cocido.oficios.domain.usecase.account.AccountUseCases(accountRepository),
    )

    private class FakeAuthRepository(
        private val user: User,
        private val onSendVerificationEmail: () -> Unit = {},
        private val verifyEmailResult: AppResult<Unit> = AppResult.Success(Unit),
    ) : AuthRepository {
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
        override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
        override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
            AppResult.Error(AppError.Unknown())
        override suspend fun sendVerificationEmail(): AppResult<String> {
            onSendVerificationEmail()
            return AppResult.Success("Código de verificación reenviado a su correo")
        }
        override suspend fun verifyEmail(code: String) = verifyEmailResult
        override suspend fun getUserRoles() = AppResult.Success(
            listOf(UserRoleDefinition(1, "Cliente", UserRole.CLIENT)),
        )
    }

    private class FakeSessionRepository : com.cocido.oficios.domain.repository.SessionRepository {
        override val authState = MutableStateFlow(
            com.cocido.oficios.domain.model.AuthState.Unauthenticated,
        )
        override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
        override suspend fun clearSession() = Unit
        override suspend fun getAccessToken() = null
        override suspend fun getRefreshToken() = null
        override suspend fun updateTokens(tokens: AuthTokens) = Unit
        override suspend fun getAccessTokenExpiresAtMillis(): Long? = null
        override suspend fun getCachedUser() = null
        override suspend fun updateCachedUser(user: User) = Unit
    }

    private class FakeAccountRepository(
        private val onUpdate: suspend (UpdateAccountProfileRequest) -> AppResult<AccountProfile> = {
            AppResult.Error(AppError.Unknown("unused"))
        },
    ) : AccountRepository {
        private val unused = AppResult.Error(AppError.Unknown("unused"))
        override val profile: StateFlow<AccountProfile?> = MutableStateFlow(null)
        override val providerDetails: StateFlow<com.cocido.oficios.domain.model.ProviderAccountDetails?> =
            MutableStateFlow(null)
        override val domicilios: StateFlow<List<com.cocido.oficios.domain.model.ClientDomicilio>?> =
            MutableStateFlow(null)
        override val contactos: StateFlow<List<com.cocido.oficios.domain.model.ClientContacto>?> =
            MutableStateFlow(null)
        override suspend fun getProfile() = unused
        override suspend fun updateProfile(request: UpdateAccountProfileRequest) = onUpdate(request)
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
}
