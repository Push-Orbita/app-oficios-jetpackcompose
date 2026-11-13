package com.cocido.oficios.domain.model

/**
 * Valores que muestra la pantalla de revisión del wizard de registro.
 *
 * El wizard de trabajador no pasa por el perfil de cliente: barrio y calle
 * viven en [RegistrationDraft.provider] (zona / local). Si la revisión lee
 * solo [RegistrationDraft.client], esos campos salen vacíos aunque ya estén
 * cargados. El barrio del trabajador sí se envía en el signup vía el draft
 * de oficio; el celular no se pide en ese flujo.
 */
data class RegistrationReviewSummary(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val barrio: String,
    val street: String,
    val document: String = "",
    val contactName: String = "",
    val piso: String = "",
    val departamento: String = "",
    val oficioCategory: String = "",
    val oficioRubro: String = "",
    val experienceRange: String = "",
    val workDays: String = "",
    val schedule: String = "",
    val serviceArea: String = "",
    val paymentMethods: String = "",
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
            document = draft.personal.document,
            contactName = draft.client.contactName,
            piso = draft.client.piso,
            departamento = draft.client.departamento,
            provider = draft.provider,
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
            document: String = "",
            contactName: String = "",
            piso: String = "",
            departamento: String = "",
            provider: RegisterTradeDraft? = null,
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
            val trade = provider.takeIf { role == RegistrationRole.PROVIDER }
            return RegistrationReviewSummary(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                barrio = barrio,
                street = street,
                document = document.trim(),
                contactName = contactName.trim(),
                piso = piso.trim(),
                departamento = departamento.trim(),
                oficioCategory = trade?.categoryName.orEmpty().trim(),
                oficioRubro = trade?.rubroName.orEmpty().trim(),
                experienceRange = trade?.experienceRange.orEmpty().trim(),
                workDays = formatReviewWorkDays(trade),
                schedule = formatReviewSchedule(trade),
                serviceArea = formatReviewServiceArea(trade),
                paymentMethods = formatReviewPaymentMethods(trade),
            )
        }

        internal fun formatReviewWorkDays(draft: RegisterTradeDraft?): String {
            if (draft == null) return ""
            val days = draft.resolvedSchedule().activeDays
                .ifEmpty { draft.workDays }
            return days.map { reviewDayLabel(it) }
                .filter { it.isNotBlank() }
                .joinToString(", ")
        }

        internal fun formatReviewSchedule(draft: RegisterTradeDraft?): String {
            if (draft == null) return ""
            return draft.resolvedSchedule().days
                .filter { it.ranges.isNotEmpty() }
                .joinToString("\n") { day ->
                    val ranges = day.ranges.joinToString(", ") { range ->
                        "${range.start} a ${range.end}"
                    }
                    "${reviewDayLabel(day.day)}: $ranges"
                }
        }

        internal fun formatReviewServiceArea(draft: RegisterTradeDraft?): String {
            if (draft == null) return ""
            return listOfNotNull(
                "En el domicilio del cliente".takeIf { draft.servesAtClientHome },
                "En local propio".takeIf { draft.servesAtLocal },
            ).joinToString(" y ")
        }

        internal fun formatReviewPaymentMethods(draft: RegisterTradeDraft?): String {
            if (draft == null) return ""
            return draft.paymentMethods.map { method ->
                when (method) {
                    ProviderPaymentMethods.EFECTIVO -> "Efectivo"
                    ProviderPaymentMethods.TRANSFERENCIA -> "Transferencia"
                    ProviderPaymentMethods.MERCADOPAGO -> "Mercado Pago"
                    else -> method
                }
            }.joinToString(", ")
        }

        private fun reviewDayLabel(raw: String): String = when (raw.trim().uppercase()) {
            "LUNES" -> "Lunes"
            "MARTES" -> "Martes"
            "MIERCOLES" -> "Miércoles"
            "JUEVES" -> "Jueves"
            "VIERNES" -> "Viernes"
            "SABADO" -> "Sábado"
            "DOMINGO" -> "Domingo"
            else -> raw.trim()
        }
    }
}