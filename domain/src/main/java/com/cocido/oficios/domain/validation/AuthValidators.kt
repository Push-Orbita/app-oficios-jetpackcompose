package com.cocido.oficios.domain.validation

object AuthValidators {
    private const val MIN_PASSWORD_LENGTH = 6
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val DNI_REGEX = Regex("^[0-9]{7,11}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El email es obligatorio"
        !EMAIL_REGEX.matches(email) -> "Ingresá un email válido"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "La contraseña es obligatoria"
        password.length < MIN_PASSWORD_LENGTH ->
            "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirmá tu contraseña"
        password != confirmation -> "Las contraseñas no coinciden"
        else -> null
    }

    fun validateName(value: String, fieldName: String): String? = when {
        value.isBlank() -> "El $fieldName es obligatorio"
        else -> null
    }

    fun validateFullName(fullName: String): String? = when {
        fullName.isBlank() -> "El nombre completo es obligatorio"
        fullName.trim().split(Regex("\\s+")).size < 2 -> "Ingresá nombre y apellido"
        else -> null
    }

    fun splitFullName(fullName: String): Pair<String, String> {
        val parts = fullName.trim().split(Regex("\\s+"), limit = 2)
        return parts[0] to parts.getOrElse(1) { "" }
    }

    fun validateDocument(document: String): String? = when {
        document.isBlank() -> "El DNI es obligatorio"
        !DNI_REGEX.matches(document) -> "Ingresá un DNI válido"
        else -> null
    }
}
