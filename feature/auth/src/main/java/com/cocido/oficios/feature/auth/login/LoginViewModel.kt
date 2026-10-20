package com.cocido.oficios.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.validation.AuthValidators
import com.cocido.oficios.domain.usecase.auth.ApplyLoginSessionPreferenceUseCase
import com.cocido.oficios.domain.usecase.auth.LoginUseCase
import com.cocido.oficios.core.common.contract.DeviceTokenRegistrar
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

internal const val LOGIN_INVALID_CREDENTIALS_MESSAGE = "Los datos de sesión no coinciden"

sealed interface LoginEffect {
    data object NavigateToOnboarding : LoginEffect
    data object NavigateToRegister : LoginEffect
    data object NavigateToForgotPassword : LoginEffect
    data object NavigateToContactSupport : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val applyLoginSessionPreferenceUseCase: ApplyLoginSessionPreferenceUseCase,
    private val deviceTokenRegistrar: DeviceTokenRegistrar,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginEffect>()
    val effects: SharedFlow<LoginEffect> = _effects.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null, bannerError = null)
                    .validated()
            }
            is LoginEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null, bannerError = null)
                    .validated()
            }
            is LoginEvent.RememberMeChanged -> _uiState.update {
                it.copy(rememberMe = event.value)
            }
            LoginEvent.Submit -> submit()
            LoginEvent.RegisterClicked -> emitEffect(LoginEffect.NavigateToRegister)
            LoginEvent.ForgotPasswordClicked -> emitEffect(LoginEffect.NavigateToForgotPassword)
            LoginEvent.ContactSupportClicked -> emitEffect(LoginEffect.NavigateToContactSupport)
        }
    }

    private fun submit() {
        val emailError = AuthValidators.validateEmail(_uiState.value.email)
        val passwordError = AuthValidators.validatePassword(_uiState.value.password)
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    bannerError = null,
                    hasAttemptedSubmit = true,
                ).validated()
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, bannerError = null) }
            when (
                val result = loginUseCase(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password,
                )
            ) {
                is AppResult.Success -> {
                    applyLoginSessionPreferenceUseCase(_uiState.value.rememberMe)
                    deviceTokenRegistrar.syncCurrentToken()
                    _uiState.update { it.copy(isLoading = false) }
                    emitEffect(LoginEffect.NavigateToOnboarding)
                }
                is AppResult.Error -> {
                    // Nunca mostramos el mensaje crudo del backend para 401/404: revelaría si
                    // el email existe o no (user enumeration) y además suena a error interno.
                    val message = when (result.error) {
                        is AppError.Unauthorized, is AppError.NotFound ->
                            LOGIN_INVALID_CREDENTIALS_MESSAGE
                        else -> result.error.userMessage()
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bannerError = message,
                            hasAttemptedSubmit = true,
                        )
                    }
                }
            }
        }
    }

    private fun emitEffect(effect: LoginEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    private fun LoginUiState.validated(): LoginUiState = copy(
        canSubmit = AuthValidators.validateEmail(email) == null &&
            AuthValidators.validatePassword(password) == null,
    )
}
