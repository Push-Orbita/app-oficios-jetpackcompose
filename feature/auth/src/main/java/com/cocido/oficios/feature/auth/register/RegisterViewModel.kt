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
            is RegisterFormEvent.FirstNameChanged -> update { it.copy(firstName = event.value) }
            is RegisterFormEvent.LastNameChanged -> update { it.copy(lastName = event.value) }
            is RegisterFormEvent.EmailChanged -> update { it.copy(email = event.value) }
            is RegisterFormEvent.PasswordChanged -> update { it.copy(password = event.value) }
            is RegisterFormEvent.ConfirmPasswordChanged -> update { it.copy(confirmPassword = event.value) }
            is RegisterFormEvent.DocumentChanged -> update { it.copy(document = event.value) }
            is RegisterFormEvent.TermsChanged -> update { it.copy(acceptedTerms = event.value) }
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
        val state = _uiState.value.copy(hasAttemptedSubmit = true).validated()
        if (!state.canSubmit) {
            _uiState.value = state
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, hasAttemptedSubmit = true) }
            when (mode) {
                RegisterMode.CLIENT -> {
                    when (
                        val result = registerClientUseCase(
                            RegisterClientRequest(
                                firstName = state.firstName.trim(),
                                lastName = state.lastName.trim(),
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
                                firstName = state.firstName.trim(),
                                lastName = state.lastName.trim(),
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

    private fun RegisterFormUiState.validated(): RegisterFormUiState {
        val firstNameError = AuthValidators.validateName(firstName, "nombres")
        val lastNameError = AuthValidators.validateName(lastName, "apellido")
        val emailError = AuthValidators.validateEmail(email)
        val passwordError = AuthValidators.validatePassword(password)
        val confirmPasswordError = AuthValidators.validatePasswordConfirmation(
            password,
            confirmPassword,
        )
        val documentError = AuthValidators.validateDocument(document)
        val termsError = if (!acceptedTerms) "Debe aceptar los términos" else null
        val canSubmit = listOf(
            firstNameError,
            lastNameError,
            emailError,
            passwordError,
            confirmPasswordError,
            documentError,
            termsError,
        ).all { it == null }
        return copy(
            firstNameError = firstNameError,
            lastNameError = lastNameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            documentError = documentError,
            termsError = termsError,
            canSubmit = canSubmit,
        )
    }
}

private fun AppError.toRegisterMessage(): String = when (this) {
    is AppError.Conflict -> message.ifBlank { "Este correo ya está registrado" }
    is AppError.Validation -> message.ifBlank { "Revise los datos ingresados" }
    else -> userMessage()
}
