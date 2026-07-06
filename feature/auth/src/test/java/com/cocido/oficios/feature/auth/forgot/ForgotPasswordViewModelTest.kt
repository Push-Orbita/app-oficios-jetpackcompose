package com.cocido.oficios.feature.auth.forgot

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.auth.ForgotPasswordUseCase
import com.cocido.oficios.feature.auth.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

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
    fun `valid email enables submit`() {
        val viewModel = ForgotPasswordViewModel(ForgotPasswordUseCase(FakeAuthRepository()))
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("user@test.com"))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `success navigates to reset`() = runTest(dispatcher) {
        val viewModel = ForgotPasswordViewModel(ForgotPasswordUseCase(FakeAuthRepository()))
        val effects = mutableListOf<ForgotPasswordEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(ForgotPasswordEvent.Submit)
        advanceUntilIdle()
        assertTrue(effects.any { it is ForgotPasswordEffect.NavigateToReset })
        job.cancel()
    }

    @Test
    fun `error shows message`() = runTest(dispatcher) {
        val customRepo = object : com.cocido.oficios.domain.repository.AuthRepository {
            override suspend fun login(email: String, password: String) = AppResult.Error(AppError.Unknown())
            override suspend fun registerClient(request: com.cocido.oficios.domain.model.RegisterClientRequest) =
                AppResult.Error(AppError.Unknown())
            override suspend fun registerProvider(request: com.cocido.oficios.domain.model.RegisterProviderRequest) =
                AppResult.Error(AppError.Unknown())
            override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown())
            override suspend fun getCurrentUser() = AppResult.Error(AppError.Unknown())
            override suspend fun logout() = AppResult.Success(Unit)
            override suspend fun requestPasswordReset(email: String) =
                AppResult.Error(AppError.Network("Sin conexión"))
            override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
            override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
                AppResult.Error(AppError.Unknown())
            override suspend fun sendVerificationEmail() = AppResult.Success("")
            override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
            override suspend fun getUserRoles(): AppResult<List<com.cocido.oficios.domain.model.UserRoleDefinition>> =
                AppResult.Success(emptyList())
        }
        val viewModel = ForgotPasswordViewModel(ForgotPasswordUseCase(customRepo))
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(ForgotPasswordEvent.Submit)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.generalError)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
