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
        assertEquals("3705219109", AuthValidators.normalizeArgentinePhone("+543705219109"))
    }

    @Test
    fun `normalizeArgentinePhone strips country code without plus`() {
        assertEquals("3705219109", AuthValidators.normalizeArgentinePhone("543705219109"))
    }

    @Test
    fun `normalizeArgentinePhone leaves plain local number untouched`() {
        assertEquals("3705246344", AuthValidators.normalizeArgentinePhone("3705246344"))
    }

    @Test
    fun `normalizeArgentinePhone leaves mobile-prefixed local number untouched`() {
        assertEquals("93705246344", AuthValidators.normalizeArgentinePhone("93705246344"))
    }

    @Test
    fun `normalizeArgentinePhone strips non-digit separators`() {
        assertEquals("93705219109", AuthValidators.normalizeArgentinePhone("+54 9 3705 219109"))
    }
}
