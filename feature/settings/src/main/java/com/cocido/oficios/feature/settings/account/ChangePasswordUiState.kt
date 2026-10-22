package com.cocido.oficios.feature.settings.account

import com.cocido.oficios.domain.validation.AuthValidators

data class ChangePasswordUiState(
    val isLoading: Boolean = true,
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
) {
    val canSubmit: Boolean
        get() = AuthValidators.validatePassword(password) == null &&
            AuthValidators.validatePasswordConfirmation(password, confirmation) == null
}

sealed interface ChangePasswordEvent {
    data class PasswordChanged(val value: String) : ChangePasswordEvent
    data class ConfirmationChanged(val value: String) : ChangePasswordEvent
    data object Submit : ChangePasswordEvent
    data object Retry : ChangePasswordEvent
    data object Back : ChangePasswordEvent
}

sealed interface ChangePasswordEffect {
    data object NavigateBack : ChangePasswordEffect
    data class Saved(val message: String) : ChangePasswordEffect
}
