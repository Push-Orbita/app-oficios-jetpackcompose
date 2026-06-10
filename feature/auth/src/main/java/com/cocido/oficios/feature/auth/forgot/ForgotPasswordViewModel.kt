package com.cocido.oficios.feature.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.auth.ForgotPasswordUseCase
import com.cocido.oficios.domain.usecase.auth.ResetPasswordUseCase
import com.cocido.oficios.domain.usecase.auth.VerifyResetCodeUseCase
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

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val generalError: String? = null,
    val canSubmit: Boolean = false,
)

data class ResetPasswordUiState(
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val codeError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val generalError: String? = null,
    val canSubmit: Boolean = false,
    val hasAttemptedSubmit: Boolean = false,
)

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent
    data object Submit : ForgotPasswordEvent
}

sealed interface ResetPasswordEvent {
    data class CodeChanged(val value: String) : ResetPasswordEvent
    data class PasswordChanged(val value: String) : ResetPasswordEvent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordEvent
    data object Submit : ResetPasswordEvent
}

sealed interface ForgotPasswordEffect {
    data object NavigateToReset : ForgotPasswordEffect
    data object NavigateToLogin : ForgotPasswordEffect
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ForgotPasswordEffect>()
    val effects: SharedFlow<ForgotPasswordEffect> = _effects.asSharedFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> _uiState.update {
                it.copy(
                    email = event.value,
                    emailError = null,
                    generalError = null,
                    canSubmit = AuthValidators.validateEmail(event.value) == null,
                )
            }
            ForgotPasswordEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val emailError = AuthValidators.validateEmail(_uiState.value.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = forgotPasswordUseCase(_uiState.value.email.trim())) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    _effects.emit(ForgotPasswordEffect.NavigateToReset)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, generalError = result.error.userMessage())
                    }
                }
            }
        }
    }
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val verifyResetCodeUseCase: VerifyResetCodeUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ForgotPasswordEffect>()
    val effects: SharedFlow<ForgotPasswordEffect> = _effects.asSharedFlow()

    private var resetToken: String? = null

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.CodeChanged -> _uiState.update {
                it.copy(code = event.value, codeError = null).validated()
            }
            is ResetPasswordEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null).validated()
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null).validated()
            }
            ResetPasswordEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val codeError = if (_uiState.value.code.isBlank()) "Ingresá el código" else null
        val passwordError = AuthValidators.validatePassword(_uiState.value.password)
        val confirmPasswordError = AuthValidators.validatePasswordConfirmation(
            _uiState.value.password,
            _uiState.value.confirmPassword,
        )
        if (codeError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    codeError = codeError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    hasAttemptedSubmit = true,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val token = resetToken ?: when (
                val verifyResult = verifyResetCodeUseCase(_uiState.value.code.trim())
            ) {
                is AppResult.Success -> verifyResult.data.also { resetToken = it }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, generalError = verifyResult.error.userMessage())
                    }
                    return@launch
                }
            }

            when (
                val result = resetPasswordUseCase(
                    resetToken = token,
                    password = _uiState.value.password,
                    confirmPassword = _uiState.value.confirmPassword,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    _effects.emit(ForgotPasswordEffect.NavigateToLogin)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, generalError = result.error.userMessage())
                    }
                }
            }
        }
    }

    private fun ResetPasswordUiState.validated(): ResetPasswordUiState = copy(
        canSubmit = code.isNotBlank() &&
            AuthValidators.validatePassword(password) == null &&
            AuthValidators.validatePasswordConfirmation(password, confirmPassword) == null,
    )
}
