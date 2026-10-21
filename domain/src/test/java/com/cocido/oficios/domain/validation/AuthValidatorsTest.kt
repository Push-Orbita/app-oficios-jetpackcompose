package com.cocido.oficios.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthValidatorsTest {

    @Test
    fun `normalizeArgentinePhone strips country code with mobile prefix`() {
        assertEquals("93705219109", AuthValidators.normalizeArgentinePhone("5493705219109"))
    }

    @Test
    fun `normalizeArgentinePhone strips plus and country code`() {
        assertEquals("93705219109", AuthValidators.normalizeArgentinePhone("+543705219109"))
    }

    @Test
    fun `normalizeArgentinePhone strips country code without plus`() {
        assertEquals("93705219109", AuthValidators.normalizeArgentinePhone("543705219109"))
    }

    @Test
    fun `normalizeArgentinePhone leaves plain local number untouched`() {
        assertEquals("93705246344", AuthValidators.normalizeArgentinePhone("3705246344"))
    }

    @Test
    fun `normalizeArgentinePhone leaves mobile-prefixed local number untouched`() {
        assertEquals("93705246344", AuthValidators.normalizeArgentinePhone("93705246344"))
    }

    @Test
    fun `normalizeArgentinePhone strips non-digit separators`() {
        assertEquals("93705219109", AuthValidators.normalizeArgentinePhone("+54 9 3705 219109"))
    }

    @Test
    fun `validateName accepts compound given names`() {
        assertEquals(null, AuthValidators.validateName("Matias Federico", "nombres"))
        assertEquals(null, AuthValidators.validateName("Britez", "apellido"))
    }

    @Test
    fun `validateName rejects blank names`() {
        assertEquals("Los nombres son obligatorios", AuthValidators.validateName("  ", "nombres"))
        assertEquals("El apellido es obligatorio", AuthValidators.validateName("", "apellido"))
    }

    @Test
    fun `email validation uses Correo copy`() {
        assertEquals("El correo es obligatorio", AuthValidators.validateEmail(""))
        assertEquals("Ingrese un correo válido", AuthValidators.validateEmail("no-es-correo"))
        assertEquals(null, AuthValidators.validateEmail("ana@oficios.test"))
    }
}
