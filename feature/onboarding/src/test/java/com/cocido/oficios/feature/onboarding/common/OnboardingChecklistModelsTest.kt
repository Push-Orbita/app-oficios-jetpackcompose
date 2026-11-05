package com.cocido.oficios.feature.onboarding.common

import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingChecklistModelsTest {

    private fun step(id: String, status: OnboardingStepStatus = OnboardingStepStatus.PENDING) =
        OnboardingStep(id = id, title = id, description = "Complete este paso.", status = status)

    private val apiSteps = listOf(
        step("dni_pendiente_revision", OnboardingStepStatus.IN_REVIEW),
        step("rubro_principal"),
        step("disponibilidad"),
        step("metodos_pago"),
        step("email_verificado"),
    )

    private fun draft(
        workDays: List<String> = emptyList(),
        paymentMethods: List<String> = emptyList(),
        subRubroIds: List<Long> = listOf(100),
    ) = RegisterTradeDraft(
        categoryId = 1,
        categoryName = "Hogar",
        rubroId = 10,
        rubroName = "Plomería",
        subRubroIds = subRubroIds,
        otherSpecialization = null,
        experienceRange = "",
        neighborhoodQuery = "",
        workDays = workDays,
        timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        paymentMethods = paymentMethods,
    )

    private fun List<OnboardingStep>.statusOf(id: String) = first { it.id == id }.status

    @Test
    fun `before kyc approval completed draft steps show saved instead of falta completar`() {
        val steps = apiSteps.withLocalDraftProgress(
            draft = draft(workDays = listOf("LUNES"), paymentMethods = listOf("EFECTIVO")),
            isIdentityVerified = false,
        )

        assertEquals(OnboardingStepStatus.SAVED, steps.statusOf("rubro_principal"))
        assertEquals(OnboardingStepStatus.SAVED, steps.statusOf("disponibilidad"))
        assertEquals(OnboardingStepStatus.SAVED, steps.statusOf("metodos_pago"))
        assertEquals(SAVED_WAITING_KYC_DESCRIPTION, steps.first { it.id == "rubro_principal" }.description)
        // Email y DNI no dependen del draft.
        assertEquals(OnboardingStepStatus.PENDING, steps.statusOf("email_verificado"))
        assertEquals(OnboardingStepStatus.IN_REVIEW, steps.statusOf("dni_pendiente_revision"))
        assertTrue(steps.hasSavedSteps())
    }

    @Test
    fun `only the parts actually saved in the draft are marked`() {
        val steps = apiSteps.withLocalDraftProgress(draft = draft(), isIdentityVerified = false)

        assertEquals(OnboardingStepStatus.SAVED, steps.statusOf("rubro_principal"))
        assertEquals(OnboardingStepStatus.PENDING, steps.statusOf("disponibilidad"))
        assertEquals(OnboardingStepStatus.PENDING, steps.statusOf("metodos_pago"))
    }

    @Test
    fun `with kyc approved the saved rubro says which step is still missing`() {
        val steps = apiSteps.withLocalDraftProgress(
            draft = draft(workDays = listOf("LUNES")),
            isIdentityVerified = true,
        )

        assertEquals(OnboardingStepStatus.SAVED, steps.statusOf("rubro_principal"))
        assertEquals(SAVED_WAITING_PAYMENT_METHODS_DESCRIPTION, steps.first { it.id == "rubro_principal" }.description)
        assertEquals(OnboardingStepStatus.PENDING, steps.statusOf("metodos_pago"))
    }

    @Test
    fun `with kyc approved and a complete draft the api status is shown as is`() {
        val steps = apiSteps.withLocalDraftProgress(
            draft = draft(workDays = listOf("LUNES"), paymentMethods = listOf("EFECTIVO")),
            isIdentityVerified = true,
        )

        assertEquals(apiSteps, steps)
        assertFalse(steps.hasSavedSteps())
    }

    @Test
    fun `without a draft nothing changes`() {
        assertEquals(apiSteps, apiSteps.withLocalDraftProgress(draft = null, isIdentityVerified = false))
        assertEquals(
            apiSteps,
            apiSteps.withLocalDraftProgress(draft = draft(subRubroIds = emptyList()), isIdentityVerified = false),
        )
    }
}
