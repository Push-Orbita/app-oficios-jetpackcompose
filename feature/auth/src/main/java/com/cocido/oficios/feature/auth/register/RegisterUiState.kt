package com.cocido.oficios.feature.auth.register

sealed interface RegisterRoleEvent {
    data object SelectClient : RegisterRoleEvent
    data object SelectProvider : RegisterRoleEvent
}

sealed interface RegisterFormEvent {
    data class FirstNameChanged(val value: String) : RegisterFormEvent
    data class LastNameChanged(val value: String) : RegisterFormEvent
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
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val document: String = "",
    val acceptedTerms: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val documentError: String? = null,
    /** Marcador no localizado; la UI muestra auth_terms_required vía stringResource. */
    val termsError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val canSubmit: Boolean = false,
    val hasAttemptedSubmit: Boolean = false,
    /** Se incrementa en cada intento fallido para re-disparar foco al primer inválido. */
    val errorFocusNonce: Int = 0,
) {
    /**
     * Error visible solo tras el primer intento de envío; después se actualiza en vivo.
     * Antes del primer intento no se muestran errores de campo (sí el checklist de contraseña).
     */
    fun visibleFieldError(error: String?, value: String = ""): String? =
        error.takeIf { hasAttemptedSubmit }

    companion object {
        /** Señal interna: no mostrar este texto; usar stringResource en la pantalla. */
        const val TERMS_REQUIRED_MARKER = "TERMS_REQUIRED"
    }
}

sealed interface RegisterEffect {
    data object NavigateToOnboarding : RegisterEffect
    data object NavigateToTradeSetup : RegisterEffect
    data object NavigateToLogin : RegisterEffect
    data object NavigateToTerms : RegisterEffect
}
