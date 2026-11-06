package com.cocido.oficios.domain.validation

enum class PasswordRequirementId {
    LENGTH,
    LETTER,
    DIGIT,
    SPECIAL,
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

/**
 * Política de contraseña de la app (fuente única para alta, reseteo y cambio de contraseña).
 *
 * Reglas (decididas tras la prueba funcional):
 * - Entre [MIN_LENGTH] y [MAX_LENGTH] caracteres.
 * - Al menos una letra (cualquier alfabeto, incluye ñ y acentos).
 * - Al menos un número.
 * - Al menos un carácter especial: cualquier carácter visible que no sea letra, número ni
 *   espacio (ver [isSpecialChar]). Ej.: ! @ # $ % & * - _ . ? /
 * - Sin espacios al inicio ni al final. Los espacios intermedios se permiten, pero no cuentan
 *   como carácter especial.
 *
 * El login NO aplica esta política: cuentas creadas con reglas anteriores tienen que poder
 * seguir entrando. El backend hoy solo valida minLength 6; hay que replicar la regla allá.
 */
object PasswordPolicy {
    const val MIN_LENGTH = 6
    const val MAX_LENGTH = 64

    const val LABEL_LENGTH = "Entre 6 y 64 caracteres"
    const val LABEL_LETTER = "Al menos una letra"
    const val LABEL_DIGIT = "Al menos un número"
    const val LABEL_SPECIAL = "Al menos un símbolo (por ej. ! @ # $ - _)"

    const val ERROR_BLANK = "Ingresá una contraseña"
    const val ERROR_EDGES = "No uses espacios al inicio ni al final"
    const val ERROR_LENGTH = "Usá entre 6 y 64 caracteres"
    const val ERROR_LETTER = "Incluí al menos una letra"
    const val ERROR_DIGIT = "Incluí al menos un número"
    const val ERROR_SPECIAL = "Incluí al menos un símbolo, por ej. ! @ # $ - _"

    /** Especial = carácter visible que no es letra, número ni espacio/control. */
    fun isSpecialChar(char: Char): Boolean =
        !char.isLetterOrDigit() && !char.isWhitespace() && !char.isISOControl()

    fun evaluate(password: String): PasswordPolicyResult {
        val hasEdgeSpaces = password.isNotEmpty() && password != password.trim()
        val lengthOk = password.length in MIN_LENGTH..MAX_LENGTH
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any(::isSpecialChar)
        val requirements = listOf(
            PasswordRequirement(PasswordRequirementId.LENGTH, LABEL_LENGTH, lengthOk),
            PasswordRequirement(PasswordRequirementId.LETTER, LABEL_LETTER, hasLetter),
            PasswordRequirement(PasswordRequirementId.DIGIT, LABEL_DIGIT, hasDigit),
            PasswordRequirement(PasswordRequirementId.SPECIAL, LABEL_SPECIAL, hasSpecial),
        )
        val error = when {
            password.isBlank() -> ERROR_BLANK
            hasEdgeSpaces -> ERROR_EDGES
            !lengthOk -> ERROR_LENGTH
            !hasLetter -> ERROR_LETTER
            !hasDigit -> ERROR_DIGIT
            !hasSpecial -> ERROR_SPECIAL
            else -> null
        }
        return PasswordPolicyResult(requirements = requirements, error = error)
    }

    fun validate(password: String): String? = evaluate(password).error
}
