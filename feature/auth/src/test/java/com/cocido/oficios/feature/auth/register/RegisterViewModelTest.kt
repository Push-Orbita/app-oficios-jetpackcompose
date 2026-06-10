package com.cocido.oficios.feature.auth.register

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.auth.RegisterClientUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterProviderUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

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
    fun `password mismatch blocks submit`() {
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(FakeAuthRepository()),
            RegisterProviderUseCase(FakeAuthRepository()),
        )
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("different"))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `invalid email blocks submit`() {
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(FakeAuthRepository()),
            RegisterProviderUseCase(FakeAuthRepository()),
        )
        viewModel.setMode(RegisterMode.CLIENT)
        viewModel.onEvent(RegisterFormEvent.EmailChanged("invalid"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `client registration success`() = runTest(dispatcher) {
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(FakeAuthRepository()),
            RegisterProviderUseCase(FakeAuthRepository()),
        )
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `provider registration requires document`() {
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(FakeAuthRepository()),
            RegisterProviderUseCase(FakeAuthRepository()),
        )
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.DocumentChanged(""))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `provider registration success with document`() = runTest(dispatcher) {
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(FakeAuthRepository()),
            RegisterProviderUseCase(FakeAuthRepository()),
        )
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun fillValidClientForm(viewModel: RegisterViewModel) {
        viewModel.onEvent(RegisterFormEvent.FullNameChanged("Juan Pérez"))
        viewModel.onEvent(RegisterFormEvent.DocumentChanged("12345678"))
        viewModel.onEvent(RegisterFormEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
    }
}
