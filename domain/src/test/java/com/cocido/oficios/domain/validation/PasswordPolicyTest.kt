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
        assertEquals("Te falta entre 6 y 64 caracteres", result.error)
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
        assertEquals("Te falta entre 6 y 64 caracteres", PasswordPolicy.validate(password))
    }

    @Test
    fun `64 characters are accepted`() {
        val password = "a".repeat(62) + "1!"
        assertEquals(PasswordPolicy.MAX_LENGTH, password.length)
        assertNull(PasswordPolicy.validate(password))
    }

    @Test
    fun `missing letter digit or symbol report aggregated message`() {
        assertEquals("Te falta al menos una letra", PasswordPolicy.validate("123456!"))
        assertEquals("Te falta al menos un número", PasswordPolicy.validate("abcdef!"))
        assertEquals("Te falta un símbolo", PasswordPolicy.validate("abc123"))
        assertFalse(PasswordPolicy.evaluate("abc123").requirement(PasswordRequirementId.SPECIAL).satisfied)
    }

    @Test
    fun `multiple missing requirements are listed with y`() {
        assertEquals(
            "Te falta entre 6 y 64 caracteres y un símbolo",
            PasswordPolicy.validate("xd123"),
        )
        assertEquals(
            "Te falta al menos un número y un símbolo",
            PasswordPolicy.validate("abcdef"),
        )
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
        assertEquals("Te falta un símbolo", PasswordPolicy.validate("sec ret1"))
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

    @Test
    fun `formatMissingMessage joins parts in voseo`() {
        assertEquals(
            "Te falta al menos un número y un símbolo",
            PasswordPolicy.formatMissingMessage(
                listOf(PasswordRequirementId.DIGIT, PasswordRequirementId.SPECIAL),
            ),
        )
    }

    private fun PasswordPolicyResult.requirement(id: PasswordRequirementId): PasswordRequirement =
        requirements.first { it.id == id }
}
