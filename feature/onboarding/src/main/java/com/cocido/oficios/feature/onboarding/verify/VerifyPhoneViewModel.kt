package com.cocido.oficios.feature.onboarding.verify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.VerificationCodeCopy
import com.cocido.oficios.core.common.result.toOtpCodeMessage
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.usecase.profile.GetClientContactosUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import com.cocido.oficios.domain.usecase.profile.SendContactPhoneVerificationCodeUseCase
import com.cocido.oficios.domain.usecase.profile.VerifyContactPhoneCodeUseCase
import com.cocido.oficios.domain.validation.AuthValidators
import com.cocido.oficios.feature.onboarding.client.PendingClientOnboardingStore
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

data class VerifyPhoneUiState(
    val telefono: String = "",
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val infoMessage: String? = null,
    val error: String? = null,
    val canSubmit: Boolean = false,
    val hasAttemptedSubmit: Boolean = false,
    val codeRejections: Int = 0,
)

sealed interface VerifyPhoneEffect {
    data object Verified : VerifyPhoneEffect
}

@HiltViewModel
class VerifyPhoneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sendContactPhoneVerificationCodeUseCase: SendContactPhoneVerificationCodeUseCase,
    private val verifyContactPhoneCodeUseCase: VerifyContactPhoneCodeUseCase,
    private val saveClientDomicilioUseCase: SaveClientDomicilioUseCase,
    private val getClientContactosUseCase: GetClientContactosUseCase,
    private val pendingClientOnboardingStore: PendingClientOnboardingStore,
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId")?.takeIf { it > 0L }
        ?: INVALID_CONTACT_ID
    private val telefonoArg: String = savedStateHandle.get<String>("telefono").orEmpty()

    private val _uiState = MutableStateFlow(VerifyPhoneUiState())
    val uiState: StateFlow<VerifyPhoneUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<VerifyPhoneEffect>()
    val effects: SharedFlow<VerifyPhoneEffect> = _effects.asSharedFlow()

    private var resolvedContactId: Long? = null
    private var resendCooldownJob: Job? = null

    init {
        viewModelScope.launch {
            resolveContact()
            sendCode()
        }
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
            _uiState.update {
                it.copy(
                    codeError = codeError,
                    hasAttemptedSubmit = true,
                    codeRejections = it.codeRejections + 1,
                )
            }
            return
        }

        val currentContactId = resolvedContactId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = verifyContactPhoneCodeUseCase(currentContactId, _uiState.value.code)) {
                is AppResult.Success -> {
                    val pendingDomicilio = pendingClientOnboardingStore.pendingDomicilio
                    if (pendingDomicilio != null) {
                        when (val domicilioResult = saveClientDomicilioUseCase(pendingDomicilio)) {
                            is AppResult.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Teléfono verificado, pero el domicilio falló: ${domicilioResult.error.userMessage()}",
                                    )
                                }
                                return@launch
                            }
                            is AppResult.Success -> pendingClientOnboardingStore.pendingDomicilio = null
                        }
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(VerifyPhoneEffect.Verified)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasAttemptedSubmit = true,
                            codeRejections = it.codeRejections + 1,
                        ).withVerifyPhoneError(result.error)
                    }
                }
            }
        }
    }

    private suspend fun resolveContact() {
        if (contactId != INVALID_CONTACT_ID) {
            resolvedContactId = contactId
            if (telefonoArg.isNotBlank()) {
                _uiState.update { it.copy(telefono = telefonoArg) }
            }
            return
        }
        when (val result = getClientContactosUseCase()) {
            is AppResult.Success -> {
                val contact = result.data.firstOrNull { it.favorito } ?: result.data.firstOrNull()
                resolvedContactId = contact?.id
                _uiState.update { it.copy(telefono = contact?.telefonoWhatsapp.orEmpty()) }
            }
            is AppResult.Error -> Unit
        }
    }

    private suspend fun sendCode() {
        val currentContactId = resolvedContactId
        if (currentContactId == null) {
            _uiState.update { it.copy(error = "No se encontró un contacto para verificar.") }
            return
        }

        _uiState.update { it.copy(isResending = true, error = null) }
        when (val result = sendContactPhoneVerificationCodeUseCase(currentContactId)) {
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        isResending = false,
                        infoMessage = result.data.message,
                    )
                }
                startResendCooldown(result.data.resendCooldownSeconds ?: DEFAULT_RESEND_COOLDOWN_SECONDS)
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isResending = false,
                        error = result.error.userMessage(),
                    )
                }
                startResendCooldown(DEFAULT_RESEND_COOLDOWN_SECONDS)
            }
        }
    }

    private fun startResendCooldown(seconds: Int) {
        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                _uiState.update { it.copy(resendCooldownSeconds = remaining) }
                delay(ONE_SECOND_MS)
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    private companion object {
        const val INVALID_CONTACT_ID = -1L
        const val DEFAULT_RESEND_COOLDOWN_SECONDS = 60
        const val ONE_SECOND_MS = 1_000L
    }
}

private fun VerifyPhoneUiState.withVerifyPhoneError(error: AppError): VerifyPhoneUiState =
    if (error is AppError.Validation) {
        copy(
            codeError = error.toOtpCodeMessage(VerificationCodeCopy.PHONE),
            error = null,
        )
    } else {
        copy(error = error.userMessage())
    }
