package com.cocido.oficios.feature.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.AccountDeletionStatus
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.usecase.account.AccountUseCases
import com.cocido.oficios.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val accountUseCases: AccountUseCases,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = Channel<DeleteAccountEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var submitIdempotencyKey: String? = null

    init {
        load()
    }

    fun onEvent(event: DeleteAccountEvent) {
        when (event) {
            DeleteAccountEvent.Retry -> load()
            is DeleteAccountEvent.PasswordChanged -> {
                submitIdempotencyKey = null
                _uiState.update { it.copy(password = event.value, passwordError = null, errorMessage = null) }
            }
            is DeleteAccountEvent.ConfirmationChanged -> {
                submitIdempotencyKey = null
                _uiState.update { it.copy(confirmation = event.value, confirmationError = null, errorMessage = null) }
            }
            DeleteAccountEvent.Submit -> submit()
            DeleteAccountEvent.CancelPending -> cancelPending()
            DeleteAccountEvent.Back -> emit(DeleteAccountEffect.NavigateBack)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = accountUseCases.getCurrentDeletionRequest()) {
                is AppResult.Success -> {
                    val pending = result.data?.takeIf { it.status == AccountDeletionStatus.PENDING_GRACE_PERIOD }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingRequest = pending,
                            scheduledForLabel = formatSchedule(pending?.scheduledForIso),
                            errorMessage = null,
                        )
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.hasPendingGrace || state.isSubmitting) return
        val passwordError = if (state.password.isBlank()) "Ingresá tu contraseña actual." else null
        val confirmationError = if (state.confirmation.trim() != CONFIRMATION_WORD) {
            "Escribí ELIMINAR para confirmar la baja."
        } else {
            null
        }
        if (passwordError != null || confirmationError != null) {
            _uiState.update { it.copy(passwordError = passwordError, confirmationError = confirmationError) }
            return
        }
        val key = submitIdempotencyKey ?: UUID.randomUUID().toString().also { submitIdempotencyKey = it }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = accountUseCases.requestAccountDeletion(
                    RequestAccountDeletionParams(
                        currentPassword = state.password,
                        confirmation = CONFIRMATION_WORD,
                        idempotencyKey = key,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    val whenLabel = formatSchedule(result.data.scheduledForIso)
                    emit(
                        DeleteAccountEffect.ShowMessage(
                            if (whenLabel != null) {
                                "Solicitamos la baja. Tenés tiempo hasta el $whenLabel para cancelarla si volvés a iniciar sesión."
                            } else {
                                "Solicitamos la baja de tu cuenta. Tenés 3 días para cancelarla si volvés a iniciar sesión."
                            },
                        ),
                    )
                    logoutUseCase()
                    emit(DeleteAccountEffect.LoggedOut)
                }
                is AppResult.Error -> {
                    if (result.error.backendCode == "ACCOUNT_DELETION_ALREADY_REQUESTED") {
                        submitIdempotencyKey = null
                        load()
                        _uiState.update { it.copy(isSubmitting = false) }
                    } else {
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = result.error.userMessage())
                        }
                    }
                }
            }
        }
    }

    private fun cancelPending() {
        if (_uiState.value.isCancelling) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null) }
            when (val result = accountUseCases.cancelAccountDeletion()) {
                is AppResult.Success -> {
                    submitIdempotencyKey = null
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            pendingRequest = null,
                            scheduledForLabel = null,
                            password = "",
                            confirmation = "",
                        )
                    }
                    emit(DeleteAccountEffect.ShowMessage("Cancelamos la solicitud de baja. Tu cuenta sigue activa."))
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isCancelling = false, errorMessage = result.error.userMessage())
                }
            }
        }
    }

    private fun emit(effect: DeleteAccountEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    companion object {
        const val CONFIRMATION_WORD = "ELIMINAR"
        private val cordobaZone = ZoneId.of("America/Argentina/Cordoba")
        private val scheduleFormatter = DateTimeFormatter.ofPattern(
            "d 'de' MMMM 'de' yyyy, HH:mm",
            Locale.forLanguageTag("es-AR"),
        )

        fun formatSchedule(iso: String?): String? {
            if (iso.isNullOrBlank()) return null
            return runCatching {
                OffsetDateTime.parse(iso)
                    .atZoneSameInstant(cordobaZone)
                    .format(scheduleFormatter)
            }.getOrNull()
        }
    }
}
