package com.cocido.oficios.feature.settings.account

import com.cocido.oficios.domain.model.AccountDeletionRequest

data class DeleteAccountUiState(
    val isLoading: Boolean = true,
    val pendingRequest: AccountDeletionRequest? = null,
    val scheduledForLabel: String? = null,
    val password: String = "",
    val confirmation: String = "",
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isCancelling: Boolean = false,
) {
    val hasPendingGrace: Boolean
        get() = pendingRequest != null
}

sealed interface DeleteAccountEvent {
    data object Retry : DeleteAccountEvent
    data class PasswordChanged(val value: String) : DeleteAccountEvent
    data class ConfirmationChanged(val value: String) : DeleteAccountEvent
    data object Submit : DeleteAccountEvent
    data object CancelPending : DeleteAccountEvent
    data object Back : DeleteAccountEvent
}

sealed interface DeleteAccountEffect {
    data object NavigateBack : DeleteAccountEffect
    data object LoggedOut : DeleteAccountEffect
    data class ShowMessage(val message: String) : DeleteAccountEffect
}
