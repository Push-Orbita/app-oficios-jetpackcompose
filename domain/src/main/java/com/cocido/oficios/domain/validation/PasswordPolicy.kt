package com.cocido.oficios.domain.validation

enum class PasswordRequirementId {
    LENGTH,
    LETTER,
    DIGIT,
}

data class PasswordRequirement(
    val id: PasswordRequirementId,
    val label: String,
    val satisfied: Boolean,
)

data class PasswordPolicyResult(
    val requirements: List<PasswordRequirement>,
    val error: String?,
) {
    val isValid: Boolean get() = error == null
}

object PasswordPolicy {
    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 64

    const val LABEL_LENGTH = "Entre 8 y 64 caracteres"
    const val LABEL_LETTER = "Al menos una letra"
    const val LABEL_DIGIT = "Al menos un número"
    const val HINT_SYMBOLS = "Los símbolos son opcionales."

    const val ERROR_BLANK = "La contraseña es obligatoria"
    const val ERROR_EDGES = "No use espacios al inicio ni al final"
    const val ERROR_LENGTH = "Use entre 8 y 64 caracteres"
    const val ERROR_LETTER = "Incluya al menos una letra"
    const val ERROR_DIGIT = "Incluya al menos un número"

    fun evaluate(password: String): PasswordPolicyResult {
        val hasEdgeSpaces = password.isNotEmpty() && password != password.trim()
        val lengthOk = password.length in MIN_LENGTH..MAX_LENGTH
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val requirements = listOf(
            PasswordRequirement(PasswordRequirementId.LENGTH, LABEL_LENGTH, lengthOk),
            PasswordRequirement(PasswordRequirementId.LETTER, LABEL_LETTER, hasLetter),
            PasswordRequirement(PasswordRequirementId.DIGIT, LABEL_DIGIT, hasDigit),
        )
        val error = when {
            password.isBlank() -> ERROR_BLANK
            hasEdgeSpaces -> ERROR_EDGES
            !lengthOk -> ERROR_LENGTH
            !hasLetter -> ERROR_LETTER
            !hasDigit -> ERROR_DIGIT
            else -> null
        }
        return PasswordPolicyResult(requirements = requirements, error = error)
    }

    fun validate(password: String): String? = evaluate(password).error
}
