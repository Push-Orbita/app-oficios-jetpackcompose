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
import org.junit.Assert.assertNull
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

    private fun createViewModel(repository: FakeAuthRepository = FakeAuthRepository()) = RegisterViewModel(
        RegisterClientUseCase(repository),
        RegisterProviderUseCase(repository),
    ) to repository

    @Test
    fun `password mismatch blocks submit`() {
        val (viewModel, _) = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("different"))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `invalid email blocks submit`() {
        val (viewModel, _) = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        viewModel.onEvent(RegisterFormEvent.EmailChanged("invalid"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `client registration success`() = runTest(dispatcher) {
        val (viewModel, repository) = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        assertTrue(viewModel.uiState.value.canSubmit)
        assertFalse(viewModel.uiState.value.isLoading)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(repository.lastClientRequest)
        assertNull(repository.lastProviderRequest)
    }

    @Test
    fun `short password shows live error and submit does not call api`() = runTest(dispatcher) {
        val (viewModel, repository) = createViewModel()
        viewModel.setMode(RegisterMode.PROVIDER)
        fillReproForm(viewModel, password = "xd123")

        val before = viewModel.uiState.value
        assertFalse(before.isLoading)
        assertFalse(before.canSubmit)
        assertFalse(before.hasAttemptedSubmit)
        assertEquals(
            "La contraseña tiene que tener al menos 6 caracteres",
            before.passwordError,
        )
        assertEquals(
            "La contraseña tiene que tener al menos 6 caracteres",
            before.visibleFieldError(before.passwordError, before.password),
        )

        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        val after = viewModel.uiState.value
        assertTrue(after.hasAttemptedSubmit)
        assertFalse(after.isLoading)
        assertFalse(after.canSubmit)
        assertEquals(
            "La contraseña tiene que tener al menos 6 caracteres",
            after.passwordError,
        )
        assertNull(repository.lastProviderRequest)
        assertNull(repository.lastClientRequest)
    }

    @Test
    fun `visibleFieldError hides empty field errors until submit attempt`() {
        val state = RegisterFormUiState(
            firstNameError = "Los nombres son obligatorios",
            password = "xd123",
            passwordError = "La contraseña tiene que tener al menos 6 caracteres",
        )
        assertNull(state.visibleFieldError(state.firstNameError, state.firstName))
        assertEquals(
            "La contraseña tiene que tener al menos 6 caracteres",
            state.visibleFieldError(state.passwordError, state.password),
        )

        val attempted = state.copy(hasAttemptedSubmit = true)
        assertEquals(
            "Los nombres son obligatorios",
            attempted.visibleFieldError(attempted.firstNameError, attempted.firstName),
        )
    }

    @Test
    fun `submit on invalid form reveals field errors`() {
        val (viewModel, _) = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        viewModel.onEvent(RegisterFormEvent.FirstNameChanged(""))
        viewModel.onEvent(RegisterFormEvent.LastNameChanged(""))
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
        assertNotNull(state.firstNameError)
        assertNotNull(state.lastNameError)
        assertNotNull(state.documentError)
        assertEquals("Los nombres son obligatorios", state.firstNameError)
        assertEquals("El apellido es obligatorio", state.lastNameError)
        assertEquals("Ingrese un DNI válido", state.documentError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `client registration sends compound names without splitting`() = runTest(dispatcher) {
        val (viewModel, repository) = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel, firstName = "Matias Federico", lastName = "Britez")
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        val request = repository.lastClientRequest
        assertNotNull(request)
        assertEquals("Matias Federico", request!!.firstName)
        assertEquals("Britez", request.lastName)
        assertNull(repository.lastProviderRequest)
    }

    @Test
    fun `provider registration sends compound names without splitting`() = runTest(dispatcher) {
        val (viewModel, repository) = createViewModel()
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel, firstName = "Matias Federico", lastName = "Britez")
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        val request = repository.lastProviderRequest
        assertNotNull(request)
        assertEquals("Matias Federico", request!!.firstName)
        assertEquals("Britez", request.lastName)
    }

    @Test
    fun `provider registration requires document`() {
        val (viewModel, _) = createViewModel()
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.DocumentChanged(""))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `provider registration success navigates to trade setup, not onboarding`() = runTest(dispatcher) {
        val (viewModel, _) = createViewModel()
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

    private fun fillValidClientForm(
        viewModel: RegisterViewModel,
        firstName: String = "Juan",
        lastName: String = "Pérez",
    ) {
        fillReproForm(
            viewModel = viewModel,
            firstName = firstName,
            lastName = lastName,
            document = "12345678",
            email = "user@test.com",
            password = "secret123",
        )
    }

    private fun fillReproForm(
        viewModel: RegisterViewModel,
        firstName: String = "testingA",
        lastName: String = "testingxd",
        document: String = "33999222",
        email: String = "aikotoba9000@gmail.com",
        password: String = "xd123",
    ) {
        viewModel.onEvent(RegisterFormEvent.FirstNameChanged(firstName))
        viewModel.onEvent(RegisterFormEvent.LastNameChanged(lastName))
        viewModel.onEvent(RegisterFormEvent.DocumentChanged(document))
        viewModel.onEvent(RegisterFormEvent.EmailChanged(email))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged(password))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged(password))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
    }
}
