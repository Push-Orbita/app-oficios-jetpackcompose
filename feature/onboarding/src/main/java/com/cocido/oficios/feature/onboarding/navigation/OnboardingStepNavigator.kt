package com.cocido.oficios.feature.onboarding.navigation

import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus

object OnboardingStepNavigator {

    private val nonActionableSteps = setOf(
        "dni_pendiente_revision",
        "email_verificado",
    )

    fun isActionable(step: OnboardingStep): Boolean =
        step.status != OnboardingStepStatus.COMPLETE &&
            step.id !in nonActionableSteps

    fun routeForStep(stepId: String): String? = when (stepId) {
        "domicilio" -> OnboardingDestination.CLIENT_DOMICILIO
        "contacto" -> OnboardingDestination.CLIENT_CONTACTO
        "dni_numero", "dni_imagenes", "dni_rechazado" -> OnboardingDestination.PROVIDER_DNI
        "descripcion", "experiencia" -> OnboardingDestination.PROVIDER_PROFESSIONAL_INFO
        "rubro_principal", "subrubros", "disponibilidad", "agenda_turnos" ->
            OnboardingDestination.PROVIDER_RUBROS
        "local_comercial" -> OnboardingDestination.PROVIDER_LOCAL
        "informacion_personal" -> OnboardingDestination.PROVIDER_PROFESSIONAL_INFO
        "finalizar_perfil" -> null
        else -> null
    }
}
