package com.cocido.oficios.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomicilioFieldValidationTest {

    @Test
    fun `blank fields stay silent until showBlank`() {
        val silent = DomicilioFieldValidation.validate(
            barrioId = null,
            barrioQuery = "",
            street = "",
            number = "",
        )
        assertNull(silent.barrioError)
        assertNull(silent.streetError)
        assertNull(silent.numberError)
        assertFalse(silent.canSave)
    }

    @Test
    fun `showBlank puts the error under each empty field`() {
        val shown = DomicilioFieldValidation.validate(
            barrioId = null,
            barrioQuery = "",
            street = "",
            number = "",
            showBlank = true,
        )
        assertEquals(DomicilioFieldValidation.BARRIO_REQUIRED, shown.barrioError)
        assertEquals(DomicilioFieldValidation.STREET_REQUIRED, shown.streetError)
        assertEquals(DomicilioFieldValidation.NUMBER_REQUIRED, shown.numberError)
        assertFalse(shown.canSave)
    }

    @Test
    fun `complete address can save`() {
        val valid = DomicilioFieldValidation.validate(
            barrioId = 7L,
            barrioQuery = "Centro",
            street = "San Martín",
            number = "123",
        )
        assertNull(valid.barrioError)
        assertNull(valid.streetError)
        assertNull(valid.numberError)
        assertTrue(valid.canSave)
    }

    @Test
    fun `missing persona blocks save even with valid fields`() {
        val invalid = DomicilioFieldValidation.validate(
            barrioId = 7L,
            barrioQuery = "Centro",
            street = "San Martín",
            number = "123",
            personaMissing = true,
        )
        assertFalse(invalid.canSave)
    }

    @Test
    fun `splitStreetAndNumber separates trailing height`() {
        assertEquals("San Martín" to "123", DomicilioFieldValidation.splitStreetAndNumber("San Martín 123"))
        assertEquals("O'Higgins" to "206", DomicilioFieldValidation.splitStreetAndNumber("O'Higgins 206"))
        assertEquals("Belgrano" to "", DomicilioFieldValidation.splitStreetAndNumber("Belgrano"))
    }
}
