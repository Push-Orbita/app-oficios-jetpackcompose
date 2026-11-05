package com.cocido.oficios.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    @Test
    fun `blank password is invalid and leaves requirements unmet`() {
        val result = PasswordPolicy.evaluate("")
        assertEquals(PasswordPolicy.ERROR_BLANK, result.error)
        assertFalse(result.isValid)
        assertEquals(
            listOf(false, false, false),
            result.requirements.map { it.satisfied },
        )
    }

    @Test
    fun `short password fails length and keeps letter and digit`() {
        val result = PasswordPolicy.evaluate("abc12")
        assertEquals(PasswordPolicy.ERROR_LENGTH, result.error)
        assertFalse(result.requirement(PasswordRequirementId.LENGTH).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.LETTER).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.DIGIT).satisfied)
    }

    @Test
    fun `password longer than 64 fails length`() {
        val result = PasswordPolicy.evaluate("a".repeat(63) + "1".repeat(2))
        assertEquals(65, ("a".repeat(63) + "1".repeat(2)).length)
        assertEquals(PasswordPolicy.ERROR_LENGTH, result.error)
        assertFalse(result.requirement(PasswordRequirementId.LENGTH).satisfied)
    }

    @Test
    fun `letters only fails digit`() {
        val result = PasswordPolicy.evaluate("abcdefgh")
        assertEquals(PasswordPolicy.ERROR_DIGIT, result.error)
        assertTrue(result.requirement(PasswordRequirementId.LENGTH).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.LETTER).satisfied)
        assertFalse(result.requirement(PasswordRequirementId.DIGIT).satisfied)
    }

    @Test
    fun `digits only fails letter`() {
        val result = PasswordPolicy.evaluate("12345678")
        assertEquals(PasswordPolicy.ERROR_LETTER, result.error)
        assertTrue(result.requirement(PasswordRequirementId.LENGTH).satisfied)
        assertFalse(result.requirement(PasswordRequirementId.LETTER).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.DIGIT).satisfied)
    }

    @Test
    fun `leading or trailing spaces are rejected`() {
        assertEquals(PasswordPolicy.ERROR_EDGES, PasswordPolicy.validate(" secret12"))
        assertEquals(PasswordPolicy.ERROR_EDGES, PasswordPolicy.validate("secret12 "))
        assertEquals(PasswordPolicy.ERROR_EDGES, PasswordPolicy.validate(" secret12 "))
    }

    @Test
    fun `internal spaces are allowed when other rules pass`() {
        assertNull(PasswordPolicy.validate("sec ret1"))
    }

    @Test
    fun `symbols are optional and a mixed password is valid`() {
        assertNull(PasswordPolicy.validate("secret12"))
        assertNull(PasswordPolicy.validate("Secret1!"))
        assertNull(PasswordPolicy.validate("ñandú-9x"))
        val result = PasswordPolicy.evaluate("Secret1!")
        assertTrue(result.isValid)
        assertTrue(result.requirements.all { it.satisfied })
    }

    @Test
    fun `64 alphanumeric characters are accepted`() {
        val password = "a".repeat(63) + "1"
        assertEquals(PasswordPolicy.MAX_LENGTH, password.length)
        assertNull(PasswordPolicy.validate(password))
    }

    @Test
    fun `8 alphanumeric characters are accepted`() {
        assertNull(PasswordPolicy.validate("abcd1234"))
    }

    @Test
    fun `validate matches evaluate error`() {
        val password = "short"
        assertEquals(PasswordPolicy.evaluate(password).error, PasswordPolicy.validate(password))
    }

    private fun PasswordPolicyResult.requirement(id: PasswordRequirementId): PasswordRequirement =
        requirements.first { it.id == id }
}
