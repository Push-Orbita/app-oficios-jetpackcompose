package com.cocido.oficios.feature.auth.register

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.auth.RegisterClientUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterProviderUseCase
import com.cocido.oficios.domain.validation.AuthValidators
import com.cocido.oficios.domain.validation.PasswordPolicy
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
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret12!"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret12!"))
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
            "Te falta entre 6 y 64 caracteres y un símbolo",
            before.passwordError,
        )
        assertNull(before.visibleFieldError(before.passwordError, before.password))

        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        val after = viewModel.uiState.value
        assertTrue(after.hasAttemptedSubmit)
        assertFalse(after.isLoading)
        assertFalse(after.canSubmit)
        assertEquals(
            "Te falta entre 6 y 64 caracteres y un símbolo",
            after.passwordError,
        )
        assertEquals(
            "Te falta entre 6 y 64 caracteres y un símbolo",
            after.visibleFieldError(after.passwordError, after.password),
        )
        assertNull(repository.lastProviderRequest)
        assertNull(repository.lastClientRequest)
    }

    @Test
    fun `visibleFieldError hides all field errors until submit attempt`() {
        val state = RegisterFormUiState(
            firstNameError = AuthValidators.FIELD_REQUIRED,
            password = "xd123",
            passwordError = "Te falta entre 6 y 64 caracteres y un símbolo",
        )
        assertNull(state.visibleFieldError(state.firstNameError, state.firstName))
        assertNull(state.visibleFieldError(state.passwordError, state.password))

        val attempted = state.copy(hasAttemptedSubmit = true)
        assertEquals(
            AuthValidators.FIELD_REQUIRED,
            attempted.visibleFieldError(attempted.firstNameError, attempted.firstName),
        )
        assertEquals(
            "Te falta entre 6 y 64 caracteres y un símbolo",
            attempted.visibleFieldError(attempted.passwordError, attempted.password),
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
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret12!"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret12!"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))

        assertFalse(viewModel.uiState.value.canSubmit)
        assertFalse(viewModel.uiState.value.hasAttemptedSubmit)

        viewModel.onEvent(RegisterFormEvent.Submit)

        val state = viewModel.uiState.value
        assertTrue(state.hasAttemptedSubmit)
        assertNotNull(state.firstNameError)
        assertNotNull(state.lastNameError)
        assertNotNull(state.documentError)
        assertEquals(AuthValidators.FIELD_REQUIRED, state.firstNameError)
        assertEquals(AuthValidators.FIELD_REQUIRED, state.lastNameError)
        assertEquals("Ingresá un DNI válido", state.documentError)
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
    fun `register va01 email field error is on the field not nest banner`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            registerClientResult = AppResult.Error(
                AppError.Validation(
                    message = "Datos de entrada no válidos.",
                    fieldErrors = mapOf("email" to "Ingrese un correo válido"),
                    backendCode = "VA01",
                ),
            ),
        )
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(repository),
            RegisterProviderUseCase(repository),
        )
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ingrese un correo válido", state.emailError)
        assertNull(state.generalError)
        assertTrue(state.hasAttemptedSubmit)
    }

    @Test
    fun `register generic va01 does not show nest copy`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            registerClientResult = AppResult.Error(
                AppError.Validation(
                    message = "Datos de entrada no válidos.",
                    backendCode = "VA01",
                ),
            ),
        )
        val viewModel = RegisterViewModel(
            RegisterClientUseCase(repository),
            RegisterProviderUseCase(repository),
        )
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(
            "No pudimos crear la cuenta. Revisá los datos e intentá de nuevo.",
            state.generalError,
        )
        assertTrue("Datos de entrada no válidos" !in (state.generalError ?: ""))
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
            password = "secret12!",
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
