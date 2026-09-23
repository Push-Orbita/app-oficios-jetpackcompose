package com.cocido.oficios.feature.settings.account

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAccountViewModelTest {

    @Test
    fun formatSchedule_usesArgentinaTimezone() {
        val label = DeleteAccountViewModel.formatSchedule("2026-09-16T12:00:00-03:00")
        assertNotNull(label)
        assertTrue(label!!.contains("16"))
        assertTrue(label.contains("12:00"))
    }
}
