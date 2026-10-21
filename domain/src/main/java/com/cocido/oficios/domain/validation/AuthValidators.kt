package com.cocido.oficios.domain.validation

object AuthValidators {
    private const val MIN_PASSWORD_LENGTH = 6
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val DNI_REGEX = Regex("^[0-9]{7,11}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El correo es obligatorio"
        !EMAIL_REGEX.matches(email) -> "Ingrese un correo válido"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "La contraseña es obligatoria"
        password.length < MIN_PASSWORD_LENGTH ->
            "La contraseña tiene que tener al menos $MIN_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirme su contraseña"
        password != confirmation -> "Las contraseñas no coinciden"
        else -> null
    }

    fun validateName(value: String, fieldName: String): String? = when {
        value.isBlank() -> when (fieldName) {
            "nombres" -> "Los nombres son obligatorios"
            "apellido" -> "El apellido es obligatorio"
            else -> "El $fieldName es obligatorio"
        }
        else -> null
    }

    fun validateDocument(document: String): String? = when {
        document.isBlank() -> "El DNI es obligatorio"
        !DNI_REGEX.matches(document) -> "Ingrese un DNI válido"
        else -> null
    }

    fun validateVerificationCode(code: String): String? = when {
        code.isBlank() -> "Ingrese el código"
        code.length != 6 || code.any { !it.isDigit() } -> "El código tiene que tener 6 dígitos"
        else -> null
    }

    /**
     * Número local de celular argentino: 11 dígitos y empieza con 9 (WhatsApp +549…).
     * Si el usuario pega +54 o tipea 10 dígitos sin el 9, se antepone el 9.
     */
    fun normalizeArgentinePhone(raw: String): String {
        val digits = raw.trim().removePrefix("+").filter(Char::isDigit)
        val local = if (digits.startsWith(ARGENTINA_COUNTRY_CODE) && digits.length - ARGENTINA_COUNTRY_CODE.length in 10..11) {
            digits.removePrefix(ARGENTINA_COUNTRY_CODE)
        } else {
            digits
        }
        return if (local.length == 10 && !local.startsWith("9")) "9$local" else local
    }

    private const val ARGENTINA_COUNTRY_CODE = "54"
}
