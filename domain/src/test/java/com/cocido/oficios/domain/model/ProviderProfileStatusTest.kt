package com.cocido.oficios.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderProfileStatusTest {

    @Test
    fun `dni approved while selfie is still in review unlocks rubro full`() {
        val status = status(
            hasDniDocuments = true,
            missingSteps = listOf("selfie_pendiente_revision", "rubro_principal", "disponibilidad"),
        )
        assertTrue(status.isDniApproved)
    }

    @Test
    fun `dni in review keeps the gate closed`() {
        val status = status(
            hasDniDocuments = true,
            missingSteps = listOf("dni_pendiente_revision", "selfie_pendiente_revision"),
        )
        assertFalse(status.isDniApproved)
    }

    @Test
    fun `without dni documents the gate stays closed`() {
        assertFalse(status(hasDniDocuments = false, missingSteps = listOf("rubro_principal")).isDniApproved)
    }

    @Test
    fun `identity verified always counts as dni approved`() {
        assertTrue(status(identityVerified = true, missingSteps = listOf("dni_pendiente_revision")).isDniApproved)
    }

    private fun status(
        identityVerified: Boolean = false,
        hasDniDocuments: Boolean = false,
        missingSteps: List<String>,
    ) = ProviderProfileStatus(
        accountStatus = AccountStatus.PENDING_APPROVAL,
        isConfigurationComplete = false,
        isProfileComplete = false,
        isOperational = false,
        isIdentityVerified = identityVerified,
        hasDniDocuments = hasDniDocuments,
        missingSteps = missingSteps,
    )
}