package com.cocido.oficios.feature.auth.register

import com.cocido.oficios.domain.usecase.auth.RegisterClientUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterProviderUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    private fun createViewModel() = RegisterViewModel(
        RegisterClientUseCase(FakeAuthRepository()),
        RegisterProviderUseCase(FakeAuthRepository()),
    )

    @Test
    fun `password mismatch blocks submit`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("different"))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `invalid email blocks submit`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        viewModel.onEvent(RegisterFormEvent.EmailChanged("invalid"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `client registration success`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `submit on invalid form reveals field errors`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        viewModel.onEvent(RegisterFormEvent.FullNameChanged("asdasd"))
        viewModel.onEvent(RegisterFormEvent.DocumentChanged("12312"))
        viewModel.onEvent(RegisterFormEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))

        assertFalse(viewModel.uiState.value.canSubmit)
        assertFalse(viewModel.uiState.value.hasAttemptedSubmit)

        viewModel.onEvent(RegisterFormEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedSubmit)
        assertNotNull(state.fullNameError)
        assertNotNull(state.documentError)
        assertEquals("Ingresá nombre y apellido", state.fullNameError)
        assertEquals("Ingresá un DNI válido", state.documentError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `provider registration requires document`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.DocumentChanged(""))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `provider registration success navigates to trade setup, not onboarding`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(effects.any { it is RegisterEffect.NavigateToTradeSetup })
        assertFalse(effects.any { it is RegisterEffect.NavigateToOnboarding })
        job.cancel()
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
