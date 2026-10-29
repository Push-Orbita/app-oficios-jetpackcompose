package com.cocido.oficios.domain.validation

data class DomicilioFieldErrors(
    val barrioError: String?,
    val streetError: String?,
    val numberError: String?,
    val canSave: Boolean,
)

object DomicilioFieldValidation {
    const val BARRIO_REQUIRED = "Seleccione un barrio de la lista."
    const val STREET_REQUIRED = "Ingrese la calle."
    const val NUMBER_REQUIRED = "Ingrese la altura."
    const val PERSONA_REQUIRED = "No pudimos identificar a qué cuenta pertenece el domicilio."

    /** Piso, departamento y casa/torre son opcionales: no entran en [validate]. */

    fun validate(
        barrioId: Long?,
        barrioQuery: String,
        street: String,
        number: String,
        personaMissing: Boolean = false,
        showBlank: Boolean = false,
    ): DomicilioFieldErrors {
        val barrioError = if (barrioId == null) BARRIO_REQUIRED else null
        val streetError = if (street.isBlank()) STREET_REQUIRED else null
        val numberError = if (number.isBlank()) NUMBER_REQUIRED else null
        return DomicilioFieldErrors(
            barrioError = barrioError.takeIf { showBlank || barrioQuery.isNotBlank() },
            streetError = streetError.takeIf { showBlank || street.isNotBlank() },
            numberError = numberError.takeIf { showBlank || number.isNotBlank() },
            canSave = barrioError == null &&
                streetError == null &&
                numberError == null &&
                !personaMissing,
        )
    }

    fun splitStreetAndNumber(street: String?): Pair<String, String> {
        val value = street.orEmpty().trim()
        val match = Regex("""^(.*?)(?:\s+(\d+[A-Za-z]?))$""").matchEntire(value)
        return if (match != null && match.groupValues[1].isNotBlank()) {
            match.groupValues[1].trim() to match.groupValues[2]
        } else {
            value to ""
        }
    }
}
