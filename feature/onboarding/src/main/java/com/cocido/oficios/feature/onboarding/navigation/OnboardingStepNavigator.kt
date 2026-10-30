package com.cocido.oficios.feature.onboarding.navigation

import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus

object OnboardingStepNavigator {

    fun isActionable(step: OnboardingStep): Boolean = when (step.status) {
        OnboardingStepStatus.PENDING -> true
        OnboardingStepStatus.REJECTED -> true
        OnboardingStepStatus.IN_REVIEW -> true
        // Guardado en el dispositivo: puede volver a abrirlo para revisar o corregir.
        OnboardingStepStatus.SAVED -> true
        else -> false
    }

    fun routeForStep(stepId: String): String? = when (stepId) {
        "email_verificado" -> OnboardingDestination.VERIFY_EMAIL
        "telefono_verificado" -> OnboardingDestination.verifyPhoneRoute(contactId = -1L)
        "domicilio", "contacto" -> OnboardingDestination.CLIENT_CONTACT_DOMICILIO
        "dni_numero", "dni_imagenes", "dni_rechazado", "dni_pendiente_revision" ->
            OnboardingDestination.PROVIDER_DNI
        "selfie_imagen", "selfie_rechazada", "selfie_pendiente_revision" ->
            OnboardingDestination.PROVIDER_SELFIE
        "descripcion", "experiencia" -> OnboardingDestination.PROVIDER_PROFESSIONAL_INFO
        "rubro_principal", "subrubros" -> OnboardingDestination.PROVIDER_RUBROS
        "disponibilidad", "agenda_turnos" -> OnboardingDestination.PROVIDER_AVAILABILITY
        "metodos_pago" -> OnboardingDestination.PROVIDER_PAYMENT_METHODS
        "local_comercial" -> OnboardingDestination.PROVIDER_LOCAL
        "informacion_personal" -> OnboardingDestination.PROVIDER_PROFESSIONAL_INFO
        "finalizar_perfil" -> null
        else -> null
    }
}
