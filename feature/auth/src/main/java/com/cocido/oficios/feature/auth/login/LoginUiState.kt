package com.cocido.oficios.feature.auth.login

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data class RememberMeChanged(val value: Boolean) : LoginEvent
    data object Submit : LoginEvent
    data object RegisterClicked : LoginEvent
    data object ContactSupportClicked : LoginEvent
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = true,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val bannerError: String? = null,
    val canSubmit: Boolean = false,
    val hasAttemptedSubmit: Boolean = false,
)
