package com.cocido.oficios.feature.onboarding.navigation

import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus

object OnboardingStepNavigator {

    private val nonActionablePendingSteps = setOf(
        "email_verificado",
    )

    fun isActionable(step: OnboardingStep): Boolean = when (step.status) {
        OnboardingStepStatus.PENDING -> step.id !in nonActionablePendingSteps
        OnboardingStepStatus.REJECTED -> true
        else -> false
    }

    fun routeForStep(stepId: String): String? = when (stepId) {
        "domicilio" -> OnboardingDestination.CLIENT_DOMICILIO
        "contacto" -> OnboardingDestination.CLIENT_CONTACTO
        "dni_numero", "dni_imagenes", "dni_rechazado" -> OnboardingDestination.PROVIDER_DNI
        "descripcion", "experiencia" -> OnboardingDestination.PROVIDER_PROFESSIONAL_INFO
        "rubro_principal", "subrubros" -> OnboardingDestination.PROVIDER_RUBROS
        "disponibilidad", "agenda_turnos" -> OnboardingDestination.PROVIDER_AVAILABILITY
        "local_comercial" -> OnboardingDestination.PROVIDER_LOCAL
        "informacion_personal" -> OnboardingDestination.PROVIDER_PROFESSIONAL_INFO
        "finalizar_perfil" -> null
        else -> null
    }
}
