package com.cocido.oficios.feature.onboarding.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.VerificationCodeCopy
import com.cocido.oficios.core.common.result.toOtpCodeMessage
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.auth.GetCurrentUserUseCase
import com.cocido.oficios.domain.usecase.auth.SendVerificationEmailUseCase
import com.cocido.oficios.domain.usecase.auth.VerifyEmailUseCase
import com.cocido.oficios.domain.validation.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VerifyEmailUiState(
    val email: String = "",
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val infoMessage: String? = null,
    val error: String? = null,
    val canSubmit: Boolean = false,
    val hasAttemptedSubmit: Boolean = false,
)

sealed interface VerifyEmailEffect {
    data object Verified : VerifyEmailEffect
}

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<VerifyEmailEffect>()
    val effects: SharedFlow<VerifyEmailEffect> = _effects.asSharedFlow()

    private var resendCooldownJob: Job? = null

    init {
        // El signup ya envía el OTP (emailSent). Reenviar acá invalidaría ese código.
        viewModelScope.launch { loadEmail() }
    }

    fun onCodeChanged(value: String) = _uiState.update {
        val sanitized = value.filter(Char::isDigit).take(6)
        it.copy(
            code = sanitized,
            codeError = null,
            error = null,
            canSubmit = AuthValidators.validateVerificationCode(sanitized) == null,
        )
    }

    fun resendCode() {
        val state = _uiState.value
        if (state.isResending || state.isLoading || state.resendCooldownSeconds > 0) return
        viewModelScope.launch { sendCode() }
    }

    fun submit() {
        val codeError = AuthValidators.validateVerificationCode(_uiState.value.code)
        if (codeError != null) {
            _uiState.update { it.copy(codeError = codeError, hasAttemptedSubmit = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = verifyEmailUseCase(_uiState.value.code)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(VerifyEmailEffect.Verified)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasAttemptedSubmit = true,
                        ).withVerifyEmailError(result.error)
                    }
                }
            }
        }
    }

    private suspend fun loadEmail() {
        when (val result = getCurrentUserUseCase()) {
            is AppResult.Success -> {
                _uiState.update { it.copy(email = result.data.email) }
            }
            is AppResult.Error -> Unit
        }
    }

    private suspend fun sendCode() {
        _uiState.update { it.copy(isResending = true, error = null) }
        when (val result = sendVerificationEmailUseCase()) {
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        isResending = false,
                        infoMessage = RESENT_INFO_MESSAGE,
                        codeError = null,
                        error = null,
                    )
                }
                startResendCooldown()
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isResending = false,
                        error = result.error.userMessage(),
                    )
                }
            }
        }
    }

    private fun startResendCooldown() {
        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            for (seconds in RESEND_COOLDOWN_SECONDS downTo 1) {
                _uiState.update { it.copy(resendCooldownSeconds = seconds) }
                delay(ONE_SECOND_MS)
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    private companion object {
        const val RESEND_COOLDOWN_SECONDS = 30
        const val ONE_SECOND_MS = 1_000L
        const val RESENT_INFO_MESSAGE =
            "Le enviamos un nuevo código. Use el del correo más reciente."
    }
}

private fun VerifyEmailUiState.withVerifyEmailError(error: AppError): VerifyEmailUiState =
    if (error is AppError.Validation) {
        copy(
            codeError = error.toOtpCodeMessage(VerificationCodeCopy.EMAIL),
            error = null,
        )
    } else {
        copy(error = error.userMessage())
    }
