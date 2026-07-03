package com.cocido.oficios.feature.onboarding.verify

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.auth.SendVerificationEmailUseCase
import com.cocido.oficios.domain.usecase.auth.VerifyEmailUseCase
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
    fun `loads email and sends code on init`() = runTest(dispatcher) {
        var sendCount = 0
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(
                user = user,
                onSendVerificationEmail = { sendCount++ },
            ),
        )

        advanceUntilIdle()

        assertEquals("user@test.com", viewModel.uiState.value.email)
        assertEquals(1, sendCount)
        assertEquals("Código de verificación reenviado a tu correo", viewModel.uiState.value.infoMessage)
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
    fun `submit shows validation error for empty code`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Ingresá el código", viewModel.uiState.value.codeError)
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

        assertEquals(2, sendCount)
        assertTrue(viewModel.uiState.value.resendCooldownSeconds > 0)

        viewModel.resendCode()
        runCurrent()
        assertEquals(2, sendCount)

        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(0, viewModel.uiState.value.resendCooldownSeconds)

        viewModel.resendCode()
        advanceUntilIdle()
        assertEquals(3, sendCount)
    }

    private fun createViewModel(
        authRepository: FakeAuthRepository = FakeAuthRepository(user),
    ): VerifyEmailViewModel = VerifyEmailViewModel(
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
        sendVerificationEmailUseCase = SendVerificationEmailUseCase(authRepository),
        verifyEmailUseCase = VerifyEmailUseCase(authRepository, FakeSessionRepository()),
    )

    private class FakeAuthRepository(
        private val user: User,
        private val onSendVerificationEmail: () -> Unit = {},
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
            return AppResult.Success("Código de verificación reenviado a tu correo")
        }
        override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
        override suspend fun getUserRoles() = AppResult.Success(
            listOf(UserRoleDefinition(1, "Cliente", UserRole.CLIENT)),
        )
    }

    private class FakeSessionRepository : com.cocido.oficios.domain.repository.SessionRepository {
        override val authState = kotlinx.coroutines.flow.MutableStateFlow(
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
}
