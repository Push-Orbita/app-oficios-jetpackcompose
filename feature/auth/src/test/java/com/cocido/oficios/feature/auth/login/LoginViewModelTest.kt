package com.cocido.oficios.feature.auth.login

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.repository.SessionPreferencesRepository
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import com.cocido.oficios.domain.usecase.auth.ApplyLoginSessionPreferenceUseCase
import com.cocido.oficios.domain.usecase.auth.LoginUseCase
import com.cocido.oficios.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: FakeAuthRepository = FakeAuthRepository()) = LoginViewModel(
        LoginUseCase(repository),
        ApplyLoginSessionPreferenceUseCase(
            sessionPreferencesRepository = object : SessionPreferencesRepository {
                override fun setSessionEphemeral(ephemeral: Boolean) = Unit
                override fun isSessionEphemeral(): Boolean = false
            },
            userPreferencesRepository = object : UserPreferencesRepository {
                override val themeMode: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
                override suspend fun setThemeMode(themeMode: ThemeMode) = Unit
                override suspend fun getRememberSession(): Boolean = true
                override suspend fun setRememberSession(remember: Boolean) = Unit
            },
        ),
    )

    @Test
    fun `invalid form cannot submit`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `valid form enables submit`() {
        val viewModel = createViewModel()
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `login success clears loading`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        viewModel.onEvent(LoginEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `remember me toggle updates state`() {
        val viewModel = createViewModel()
        viewModel.onEvent(LoginEvent.RememberMeChanged(false))
        assertFalse(viewModel.uiState.value.rememberMe)
    }

    @Test
    fun `forgot password emits navigation effect`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }
        viewModel.onEvent(LoginEvent.ForgotPasswordClicked)
        advanceUntilIdle()
        assertTrue(effects.any { it is LoginEffect.NavigateToForgotPassword })
        job.cancel()
    }

    @Test
    fun `login error shows message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            loginResult = AppResult.Error(AppError.Unauthorized("Credenciales inválidas")),
        )
        val viewModel = createViewModel(repository)
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        viewModel.onEvent(LoginEvent.Submit)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.bannerError)
    }
}
