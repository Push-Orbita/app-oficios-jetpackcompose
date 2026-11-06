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
            listOf(false, false, false, false),
            result.requirements.map { it.satisfied },
        )
    }

    @Test
    fun `requirements are listed in a stable order`() {
        assertEquals(
            listOf(
                PasswordRequirementId.LENGTH,
                PasswordRequirementId.LETTER,
                PasswordRequirementId.DIGIT,
                PasswordRequirementId.SPECIAL,
            ),
            PasswordPolicy.evaluate("").requirements.map { it.id },
        )
    }

    @Test
    fun `five characters fail length and keep the rest live`() {
        val result = PasswordPolicy.evaluate("ab1!c")
        assertEquals(PasswordPolicy.ERROR_LENGTH, result.error)
        assertFalse(result.requirement(PasswordRequirementId.LENGTH).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.LETTER).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.DIGIT).satisfied)
        assertTrue(result.requirement(PasswordRequirementId.SPECIAL).satisfied)
    }

    @Test
    fun `six characters with letter digit and symbol are accepted`() {
        assertNull(PasswordPolicy.validate("abc1!x"))
        assertTrue(PasswordPolicy.evaluate("abc1!x").requirements.all { it.satisfied })
    }

    @Test
    fun `password longer than 64 fails length`() {
        val password = "a".repeat(62) + "1!" + "b"
        assertEquals(65, password.length)
        assertEquals(PasswordPolicy.ERROR_LENGTH, PasswordPolicy.validate(password))
    }

    @Test
    fun `64 characters are accepted`() {
        val password = "a".repeat(62) + "1!"
        assertEquals(PasswordPolicy.MAX_LENGTH, password.length)
        assertNull(PasswordPolicy.validate(password))
    }

    @Test
    fun `missing letter digit or symbol report the right error`() {
        assertEquals(PasswordPolicy.ERROR_LETTER, PasswordPolicy.validate("123456!"))
        assertEquals(PasswordPolicy.ERROR_DIGIT, PasswordPolicy.validate("abcdef!"))
        assertEquals(PasswordPolicy.ERROR_SPECIAL, PasswordPolicy.validate("abc123"))
        assertFalse(PasswordPolicy.evaluate("abc123").requirement(PasswordRequirementId.SPECIAL).satisfied)
    }

    @Test
    fun `any visible non alphanumeric character counts as symbol`() {
        listOf('!', '@', '#', '$', '%', '&', '*', '-', '_', '.', '?', '/', '+', '=', '¿', '¡', '€', '°').forEach {
            assertTrue("$it debería contar como símbolo", PasswordPolicy.isSpecialChar(it))
        }
    }

    @Test
    fun `letters with accents and digits are not symbols`() {
        listOf('a', 'Z', 'ñ', 'á', 'Ü', '0', '9').forEach {
            assertFalse("$it no es símbolo", PasswordPolicy.isSpecialChar(it))
        }
        assertNull(PasswordPolicy.validate("ñandú-9x"))
    }

    @Test
    fun `spaces do not count as symbol`() {
        assertFalse(PasswordPolicy.isSpecialChar(' '))
        assertEquals(PasswordPolicy.ERROR_SPECIAL, PasswordPolicy.validate("sec ret1"))
    }

    @Test
    fun `internal spaces are allowed when other rules pass`() {
        assertNull(PasswordPolicy.validate("sec ret1!"))
    }

    @Test
    fun `leading or trailing spaces are rejected`() {
        assertEquals(PasswordPolicy.ERROR_EDGES, PasswordPolicy.validate(" secret1!"))
        assertEquals(PasswordPolicy.ERROR_EDGES, PasswordPolicy.validate("secret1! "))
    }

    @Test
    fun `validate matches evaluate error`() {
        val password = "short"
        assertEquals(PasswordPolicy.evaluate(password).error, PasswordPolicy.validate(password))
    }

    private fun PasswordPolicyResult.requirement(id: PasswordRequirementId): PasswordRequirement =
        requirements.first { it.id == id }
}
