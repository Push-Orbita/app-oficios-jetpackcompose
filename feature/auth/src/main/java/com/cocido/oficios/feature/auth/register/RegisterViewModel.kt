package com.cocido.oficios.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.usecase.auth.RegisterClientUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterProviderUseCase
import com.cocido.oficios.domain.validation.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterMode { CLIENT, PROVIDER }

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerClientUseCase: RegisterClientUseCase,
    private val registerProviderUseCase: RegisterProviderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterFormUiState())
    val uiState: StateFlow<RegisterFormUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<RegisterEffect>()
    val effects: SharedFlow<RegisterEffect> = _effects.asSharedFlow()

    var mode: RegisterMode = RegisterMode.CLIENT
        private set

    fun setMode(registerMode: RegisterMode) {
        mode = registerMode
    }

    fun onEvent(event: RegisterFormEvent) {
        when (event) {
            is RegisterFormEvent.FullNameChanged -> update { it.copy(fullName = event.value, fullNameError = null) }
            is RegisterFormEvent.EmailChanged -> update { it.copy(email = event.value, emailError = null) }
            is RegisterFormEvent.PasswordChanged -> update { it.copy(password = event.value, passwordError = null) }
            is RegisterFormEvent.ConfirmPasswordChanged -> update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null)
            }
            is RegisterFormEvent.DocumentChanged -> update { it.copy(document = event.value, documentError = null) }
            is RegisterFormEvent.TermsChanged -> update { it.copy(acceptedTerms = event.value, termsError = null) }
            RegisterFormEvent.Submit -> submit()
            RegisterFormEvent.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
            RegisterFormEvent.TermsClicked -> emitEffect(RegisterEffect.NavigateToTerms)
        }
    }

    private fun emitEffect(effect: RegisterEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    private fun update(block: (RegisterFormUiState) -> RegisterFormUiState) {
        _uiState.update { block(it).validated() }
    }

    private fun submit() {
        val state = _uiState.value
        val errors = validate(state)
        if (errors != null) {
            _uiState.update { errors.copy(hasAttemptedSubmit = true).validated() }
            return
        }

        val (firstName, lastName) = AuthValidators.splitFullName(state.fullName)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            when (mode) {
                RegisterMode.CLIENT -> {
                    when (
                        val result = registerClientUseCase(
                            RegisterClientRequest(
                                firstName = firstName,
                                lastName = lastName,
                                email = state.email.trim(),
                                password = state.password,
                                document = state.document.trim(),
                            ),
                        )
                    ) {
                        is AppResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _effects.emit(RegisterEffect.NavigateToOnboarding)
                        }
                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalError = result.error.toRegisterMessage(),
                                    hasAttemptedSubmit = true,
                                )
                            }
                        }
                    }
                }
                RegisterMode.PROVIDER -> {
                    when (
                        val result = registerProviderUseCase(
                            RegisterProviderRequest(
                                firstName = firstName,
                                lastName = lastName,
                                email = state.email.trim(),
                                password = state.password,
                                document = state.document.trim(),
                            ),
                        )
                    ) {
                        is AppResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _effects.emit(RegisterEffect.NavigateToTradeSetup)
                        }
                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalError = result.error.toRegisterMessage(),
                                    hasAttemptedSubmit = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validate(state: RegisterFormUiState): RegisterFormUiState? {
        val fullNameError = AuthValidators.validateFullName(state.fullName)
        val emailError = AuthValidators.validateEmail(state.email)
        val passwordError = AuthValidators.validatePassword(state.password)
        val confirmPasswordError = AuthValidators.validatePasswordConfirmation(
            state.password,
            state.confirmPassword,
        )
        val documentError = AuthValidators.validateDocument(state.document)
        val termsError = if (!state.acceptedTerms) "Debe aceptar los términos" else null

        return if (
            listOf(fullNameError, emailError, passwordError, confirmPasswordError, documentError, termsError)
                .any { it != null }
        ) {
            state.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                documentError = documentError,
                termsError = termsError,
            )
        } else {
            null
        }
    }

    private fun RegisterFormUiState.validated(): RegisterFormUiState {
        val baseValid = AuthValidators.validateFullName(fullName) == null &&
            AuthValidators.validateEmail(email) == null &&
            AuthValidators.validatePassword(password) == null &&
            AuthValidators.validatePasswordConfirmation(password, confirmPassword) == null &&
            AuthValidators.validateDocument(document) == null &&
            acceptedTerms
        return copy(canSubmit = baseValid)
    }
}

private fun AppError.toRegisterMessage(): String = when (this) {
    is AppError.Conflict -> message.ifBlank { "Este correo ya está registrado" }
    is AppError.Validation -> message.ifBlank { "Revise los datos ingresados" }
    else -> userMessage()
}
