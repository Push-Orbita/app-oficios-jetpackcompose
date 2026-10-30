package com.cocido.oficios.feature.onboarding.navigation

import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStepNavigatorTest {

    @Test
    fun `maps client domicilio and contacto to unified step`() {
        assertEquals(
            OnboardingDestination.CLIENT_CONTACT_DOMICILIO,
            OnboardingStepNavigator.routeForStep("domicilio"),
        )
        assertEquals(
            OnboardingDestination.CLIENT_CONTACT_DOMICILIO,
            OnboardingStepNavigator.routeForStep("contacto"),
        )
    }

    @Test
    fun `maps verify email step`() {
        assertEquals(
            OnboardingDestination.VERIFY_EMAIL,
            OnboardingStepNavigator.routeForStep("email_verificado"),
        )
    }

    @Test
    fun `email verified pending step is actionable`() {
        val step = OnboardingStep(
            id = "email_verificado",
            title = "Email",
            description = "Verificar",
            status = OnboardingStepStatus.PENDING,
        )
        assertTrue(OnboardingStepNavigator.isActionable(step))
    }

    @Test
    fun `dni pending revision is actionable for corrections`() {
        val step = OnboardingStep(
            id = "dni_pendiente_revision",
            title = "DNI en revisión",
            description = "Espere",
            status = OnboardingStepStatus.IN_REVIEW,
        )
        assertTrue(OnboardingStepNavigator.isActionable(step))
        assertEquals(
            OnboardingDestination.PROVIDER_DNI,
            OnboardingStepNavigator.routeForStep("dni_pendiente_revision"),
        )
        assertEquals(
            OnboardingDestination.PROVIDER_SELFIE,
            OnboardingStepNavigator.routeForStep("selfie_pendiente_revision"),
        )
    }

    @Test
    fun `finalizar perfil has no route`() {
        assertEquals(null, OnboardingStepNavigator.routeForStep("finalizar_perfil"))
    }

    @Test
    fun `maps provider availability steps`() {
        assertEquals(
            OnboardingDestination.PROVIDER_AVAILABILITY,
            OnboardingStepNavigator.routeForStep("disponibilidad"),
        )
        assertEquals(
            OnboardingDestination.PROVIDER_AVAILABILITY,
            OnboardingStepNavigator.routeForStep("agenda_turnos"),
        )
    }

    @Test
    fun `maps provider rubro steps separately from availability`() {
        assertEquals(
            OnboardingDestination.PROVIDER_RUBROS,
            OnboardingStepNavigator.routeForStep("rubro_principal"),
        )
        assertEquals(
            OnboardingDestination.PROVIDER_RUBROS,
            OnboardingStepNavigator.routeForStep("subrubros"),
        )
    }

    @Test
    fun `maps provider payment methods route`() {
        assertEquals(
            OnboardingDestination.PROVIDER_PAYMENT_METHODS,
            OnboardingStepNavigator.routeForStep("metodos_pago"),
        )
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

    @Test
    fun `saved step stays actionable so the worker can review it`() {
        val step = OnboardingStep(
            id = "disponibilidad",
            title = "Disponibilidad",
            description = "Guardado",
            status = OnboardingStepStatus.SAVED,
        )
        assertTrue(OnboardingStepNavigator.isActionable(step))
    }

    @Test
    fun `maps provider selfie route`() {
        assertEquals(
            OnboardingDestination.PROVIDER_SELFIE,
            OnboardingStepNavigator.routeForStep("selfie_imagen"),
        )
    }

    @Test
    fun `maps verify phone route`() {
        assertTrue(
            OnboardingStepNavigator.routeForStep("telefono_verificado")
                ?.startsWith("onboarding/verify-phone/") == true,
        )
    }
}
