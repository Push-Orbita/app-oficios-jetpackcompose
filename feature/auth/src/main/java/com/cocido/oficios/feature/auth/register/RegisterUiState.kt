package com.cocido.oficios.feature.auth.register

sealed interface RegisterRoleEvent {
    data object SelectClient : RegisterRoleEvent
    data object SelectProvider : RegisterRoleEvent
}

sealed interface RegisterFormEvent {
    data class FullNameChanged(val value: String) : RegisterFormEvent
    data class EmailChanged(val value: String) : RegisterFormEvent
    data class PasswordChanged(val value: String) : RegisterFormEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterFormEvent
    data class DocumentChanged(val value: String) : RegisterFormEvent
    data class TermsChanged(val value: Boolean) : RegisterFormEvent
    data object Submit : RegisterFormEvent
    data object LoginClicked : RegisterFormEvent
    data object TermsClicked : RegisterFormEvent
}

data class RegisterFormUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val document: String = "",
    val acceptedTerms: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val documentError: String? = null,
    val termsError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val hasAttemptedSubmit: Boolean = false,
)

sealed interface RegisterEffect {
    data object NavigateToOnboarding : RegisterEffect
    data object NavigateToTradeSetup : RegisterEffect
    data object NavigateToLogin : RegisterEffect
    data object NavigateToTerms : RegisterEffect
}
