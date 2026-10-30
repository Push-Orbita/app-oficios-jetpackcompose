package com.cocido.oficios.domain.model

enum class OnboardingStepStatus {
    PENDING,
    IN_REVIEW,
    COMPLETE,
    BLOCKED,
    REJECTED,

    /**
     * Completado en el dispositivo (draft local) pero la API todavía lo lista en `faltantes`
     * porque el envío espera otra condición (p. ej. DNI aprobado o métodos de pago).
     */
    SAVED,
}

data class OnboardingStep(
    val id: String,
    val title: String,
    val description: String,
    val status: OnboardingStepStatus,
    val isRequired: Boolean = true,
)

data class ClientProfileStatus(
    val isComplete: Boolean,
    val missingSteps: List<String>,
    val hasContact: Boolean,
    val hasAddress: Boolean,
    val emailVerified: Boolean,
    val phoneVerified: Boolean = false,
    val accountStatus: AccountStatus,
)

data class ProviderProfileStatus(
    val accountStatus: AccountStatus,
    val isConfigurationComplete: Boolean,
    val isProfileComplete: Boolean,
    val isOperational: Boolean,
    val isIdentityVerified: Boolean = false,
    /** `requisitos.tieneDniNumero && tieneDniImagenes`: el DNI ya fue cargado. */
    val hasDniDocuments: Boolean = false,
    val missingSteps: List<String>,
    val rejectionReason: String? = null,
    /** `requisitos.requisitosOnboardingCompletos` de `estado-perfil`. */
    val isOnboardingRequirementsComplete: Boolean = false,
) {
    /**
     * Requisitos obligatorios del onboarding cumplidos según la API: `perfilCompleto`, o
     * `requisitosOnboardingCompletos` sin `faltantes`. Galería, local y certificados son
     * opcionales y no cuentan acá.
     */
    val hasCompletedRequiredProfile: Boolean
        get() = isProfileComplete || (isOnboardingRequirementsComplete && missingSteps.isEmpty())

    /**
     * DNI aprobado, aunque la selfie siga en revisión. `identidadVerificada` exige DNI **y** selfie,
     * pero `POST /trabajador-oficio-rubro/full` solo necesita el DNI aprobado: esperar la selfie
     * dejaba al trabajador trabado en el checklist con rubro y disponibilidad sin enviar.
     */
    val isDniApproved: Boolean
        get() = isIdentityVerified ||
            (hasDniDocuments && missingSteps.none { it in DNI_BLOCKING_STEPS })

    private companion object {
        val DNI_BLOCKING_STEPS = setOf(
            "dni_numero",
            "dni_imagenes",
            "dni_pendiente_revision",
            "dni_rechazado",
        )
    }
}
