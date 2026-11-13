package com.cocido.oficios.feature.auth.wizard

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contraseña del wizard solo en memoria de proceso.
 * Sobrevive a la destrucción del [RegistrationWizardViewModel] al volver al selector de rol,
 * pero se pierde tras process death (nunca va al draft ni a disco).
 */
@Singleton
class RegistrationPasswordHolder @Inject constructor() {
    @Volatile
    var password: String = ""
        private set

    @Volatile
    var confirmPassword: String = ""
        private set

    fun setPassword(value: String) {
        password = value
    }

    fun setConfirmPassword(value: String) {
        confirmPassword = value
    }

    fun clear() {
        password = ""
        confirmPassword = ""
    }
}
