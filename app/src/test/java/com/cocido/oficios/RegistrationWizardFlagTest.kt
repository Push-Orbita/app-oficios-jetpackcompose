package com.cocido.oficios

import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationWizardFlagTest {

    @Test
    fun registrationWizardV2IsEnabled() {
        assertTrue(BuildConfig.REGISTRATION_WIZARD_V2)
    }
}
