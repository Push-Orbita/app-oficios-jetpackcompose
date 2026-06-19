package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilEstadoDto
import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilEstadoRequisitosDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorPerfilEstadoDto
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.ProviderProfileStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingMapperTest {

    @Test
    fun `maps client status to onboarding steps`() {
        val status = ClientePerfilEstadoDto(
            perfilCompleto = false,
            faltantes = listOf("domicilio", "contacto"),
            requisitos = ClientePerfilEstadoRequisitosDto(
                tieneDomicilio = false,
                tieneContacto = false,
                emailVerificado = false,
            ),
        ).toDomain(AccountStatus.ACTIVE)

        val steps = status.toOnboardingSteps()

        assertEquals("domicilio", steps[0].id)
        assertEquals(OnboardingStepStatus.PENDING, steps[0].status)
        assertEquals("contacto", steps[1].id)
        assertTrue(steps.any { it.id == "email_verificado" })
    }

    @Test
    fun `maps provider missing steps including dni and rubro`() {
        val status = TrabajadorPerfilEstadoDto(
            estado = "PENDIENTE",
            perfilCompleto = false,
            configuracionCompleta = false,
            faltantes = listOf("dni_pendiente_revision", "rubro_principal"),
        ).toDomain()

        val steps = ProviderProfileStatus(
            accountStatus = status.accountStatus,
            isConfigurationComplete = status.isConfigurationComplete,
            isProfileComplete = status.isProfileComplete,
            isOperational = status.isOperational,
            missingSteps = status.missingSteps,
        ).toOnboardingSteps()

        assertTrue(steps.any { it.id == "dni_pendiente_revision" })
        assertEquals(
            OnboardingStepStatus.IN_REVIEW,
            steps.first { it.id == "dni_pendiente_revision" }.status,
        )
        assertTrue(steps.any { it.id == "rubro_principal" })
    }

    @Test
    fun `maps provider account status VALIDADO`() {
        assertEquals(AccountStatus.APPROVED, "VALIDADO".toProviderAccountStatus())
        assertEquals(AccountStatus.PENDING_APPROVAL, "PENDIENTE".toProviderAccountStatus())
    }

    @Test
    fun `completed client address marks domicilio complete`() {
        val status = ClientePerfilEstadoDto(
            perfilCompleto = false,
            requisitos = ClientePerfilEstadoRequisitosDto(
                tieneDomicilio = true,
                tieneContacto = false,
                emailVerificado = true,
            ),
        ).toDomain(AccountStatus.ACTIVE)

        val domicilioStep = status.toOnboardingSteps().first { it.id == "domicilio" }
        assertEquals(OnboardingStepStatus.COMPLETE, domicilioStep.status)
        assertFalse(status.toOnboardingSteps().any { it.id == "email_verificado" })
    }

    @Test
    fun `maps provider payment methods step`() {
        val steps = ProviderProfileStatus(
            accountStatus = com.cocido.oficios.domain.model.AccountStatus.PENDING_APPROVAL,
            isConfigurationComplete = false,
            isProfileComplete = false,
            isOperational = false,
            missingSteps = listOf("metodos_pago"),
        ).toOnboardingSteps()

        assertTrue(steps.any { it.id == "metodos_pago" })
    }
}
