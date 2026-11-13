package com.cocido.oficios.domain.model

/**
 * Valores que muestra "Revise sus datos".
 *
 * El wizard de trabajador no pasa por el perfil de cliente: barrio y calle
 * viven en [RegistrationDraft.provider] (zona / local). Si la revisión lee
 * solo [RegistrationDraft.client], esos campos salen vacíos ("—") aunque
 * ya estén cargados. El barrio del trabajador sí se envía en el signup
 * vía el draft de oficio; el celular no se pide en ese flujo.
 */
data class RegistrationReviewSummary(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val barrio: String,
    val street: String,
) {
    companion object {
        fun from(draft: RegistrationDraft): RegistrationReviewSummary = fromParts(
            role = draft.role,
            firstName = draft.personal.firstName,
            lastName = draft.personal.lastName,
            email = draft.personal.email,
            phone = draft.client.phone,
            clientBarrio = draft.client.barrioQuery,
            clientCalle = draft.client.calle,
            clientNumero = draft.client.numero,
            providerBarrio = draft.provider?.neighborhoodQuery,
            providerCalle = draft.provider?.localCalle,
            providerNumero = draft.provider?.localNumero,
        )

        fun fromParts(
            role: RegistrationRole,
            firstName: String,
            lastName: String,
            email: String,
            phone: String,
            clientBarrio: String,
            clientCalle: String,
            clientNumero: String,
            providerBarrio: String? = null,
            providerCalle: String? = null,
            providerNumero: String? = null,
        ): RegistrationReviewSummary {
            val clientStreet = listOf(clientCalle, clientNumero)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val providerStreet = listOf(providerCalle, providerNumero)
                .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
                .joinToString(" ")
            val barrio = when (role) {
                RegistrationRole.PROVIDER ->
                    providerBarrio?.trim().orEmpty().ifBlank { clientBarrio.trim() }
                RegistrationRole.CLIENT -> clientBarrio.trim()
            }
            val street = when (role) {
                RegistrationRole.PROVIDER -> providerStreet.ifBlank { clientStreet }
                RegistrationRole.CLIENT -> clientStreet
            }
            return RegistrationReviewSummary(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                barrio = barrio,
                street = street,
            )
        }
    }
}
