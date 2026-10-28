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

    @Test
    fun `validateArgentineMobile accepts local 10 digits and 11 with 9`() {
        assertEquals(null, AuthValidators.validateArgentineMobile("3705219109"))
        assertEquals(null, AuthValidators.validateArgentineMobile("9 370 5219109"))
        assertEquals(null, AuthValidators.validateArgentineMobile("+54 9 3705 219109"))
    }

    @Test
    fun `validateArgentineMobile rejects blank and short numbers`() {
        assertEquals("Ingrese su celular de WhatsApp.", AuthValidators.validateArgentineMobile(""))
        assertEquals(
            "Ingrese el celular con el 9 de móvil, sin +54. Ejemplo: 9 370 5219109.",
            AuthValidators.validateArgentineMobile("123"),
        )
    }

    @Test
    fun `validateContactName rejects blank and too long`() {
        assertEquals(
            "Ingrese un nombre para identificar este contacto.",
            AuthValidators.validateContactName("  "),
        )
        assertEquals(null, AuthValidators.validateContactName("Daniel"))
        assertEquals(
            "El nombre no puede superar 100 caracteres.",
            AuthValidators.validateContactName("a".repeat(101)),
        )
    }

    @Test
    fun `validateOptionalEmail allows blank`() {
        assertEquals(null, AuthValidators.validateOptionalEmail(""))
        assertEquals(null, AuthValidators.validateOptionalEmail("ana@oficios.test"))
        assertEquals("Ingrese un correo válido", AuthValidators.validateOptionalEmail("mal"))
    }
}
