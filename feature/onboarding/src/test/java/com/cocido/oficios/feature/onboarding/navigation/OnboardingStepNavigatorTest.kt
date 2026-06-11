package com.cocido.oficios.feature.onboarding.navigation

import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepNavigatorTest {

    @Test
    fun `maps client domicilio step`() {
        assertEquals(
            OnboardingDestination.CLIENT_DOMICILIO,
            OnboardingStepNavigator.routeForStep("domicilio"),
        )
    }

    @Test
    fun `email verified is not actionable`() {
        val step = OnboardingStep(
            id = "email_verificado",
            title = "Email",
            description = "Verificar",
            status = OnboardingStepStatus.PENDING,
        )
        assertFalse(OnboardingStepNavigator.isActionable(step))
    }

    @Test
    fun `dni pending revision is not actionable`() {
        val step = OnboardingStep(
            id = "dni_pendiente_revision",
            title = "DNI en revisión",
            description = "Esperá",
            status = OnboardingStepStatus.PENDING,
        )
        assertFalse(OnboardingStepNavigator.isActionable(step))
    }

    @Test
    fun `finalizar perfil has no route`() {
        assertEquals(null, OnboardingStepNavigator.routeForStep("finalizar_perfil"))
    }

    @Test
    fun `pending rubro step is actionable`() {
        val step = OnboardingStep(
            id = "rubro_principal",
            title = "Rubro",
            description = "Seleccionar",
            status = OnboardingStepStatus.PENDING,
        )
        assertTrue(OnboardingStepNavigator.isActionable(step))
    }
}
