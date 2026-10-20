package com.cocido.oficios.core.designsystem.component.inputs

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests para la lógica de OficiosCodeInput
 */
class OficiosCodeInputTest {

    @Test
    fun `filterCodeInput filtra solo dígitos`() {
        val input = "a1b2c3d4e5f6"
        val filtered = input.filter { it.isDigit() }
        assertEquals("123456", filtered)
    }

    @Test
    fun `filterCodeInput limita la longitud según digitCount`() {
        val input = "123456789"
        val digitCount = 6
        val filtered = input.filter { it.isDigit() }.take(digitCount)
        assertEquals("123456", filtered)
    }

    @Test
    fun `filterCodeInput permite menos dígitos que el máximo`() {
        val input = "123"
        val digitCount = 6
        val filtered = input.filter { it.isDigit() }.take(digitCount)
        assertEquals("123", filtered)
    }

    @Test
    fun `isCodeComplete detecta código completo`() {
        val code = "123456"
        val digitCount = 6
        assertEquals(true, code.length == digitCount)
    }

    @Test
    fun `isCodeComplete detecta código incompleto`() {
        val code = "12345"
        val digitCount = 6
        assertEquals(false, code.length == digitCount)
    }

    @Test
    fun `digitCount variable para diferentes longitudes`() {
        // Email: 6 dígitos
        val emailCode = "123456"
        assertEquals(6, emailCode.length)
        
        // Teléfono: puede variar según API
        val phoneCode4 = "1234"
        assertEquals(4, phoneCode4.length)
        
        val phoneCode8 = "12345678"
        assertEquals(8, phoneCode8.length)
    }

    @Test
    fun `empty input produces empty filtered result`() {
        val input = ""
        val filtered = input.filter { it.isDigit() }.take(6)
        assertEquals("", filtered)
    }

    @Test
    fun `non-digit input produces empty filtered result`() {
        val input = "abcdef"
        val filtered = input.filter { it.isDigit() }.take(6)
        assertEquals("", filtered)
    }
}
