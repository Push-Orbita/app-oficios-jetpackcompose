package com.cocido.oficios.feature.auth.wizard

import com.cocido.oficios.feature.auth.register.RegisterFormUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationWizardBackTest {

    @Test
    fun emptyPersonalDoesNotConfirmLeave() {
        assertFalse(hasPersonalDraftForLeaveConfirmation(RegisterFormUiState()))
    }

    @Test
    fun filledPersonalConfirmsLeaveOfWizard() {
        assertTrue(hasPersonalDraftForLeaveConfirmation(RegisterFormUiState(firstName = "Ana")))
        assertTrue(hasPersonalDraftForLeaveConfirmation(RegisterFormUiState(email = "ana@test.com")))
        assertTrue(hasPersonalDraftForLeaveConfirmation(RegisterFormUiState(acceptedTerms = true)))
    }
}