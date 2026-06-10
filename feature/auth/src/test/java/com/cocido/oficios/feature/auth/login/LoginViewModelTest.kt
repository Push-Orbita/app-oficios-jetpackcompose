package com.cocido.oficios.feature.auth.login

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.auth.LoginUseCase
import com.cocido.oficios.feature.auth.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `invalid form cannot submit`() {
        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepository()))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `valid form enables submit`() {
        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepository()))
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `login success clears loading`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(LoginUseCase(FakeAuthRepository()))
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        viewModel.onEvent(LoginEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login error shows message`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            loginResult = AppResult.Error(AppError.Unauthorized("Credenciales inválidas")),
        )
        val viewModel = LoginViewModel(LoginUseCase(repository))
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        viewModel.onEvent(LoginEvent.Submit)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.bannerError)
    }
}
