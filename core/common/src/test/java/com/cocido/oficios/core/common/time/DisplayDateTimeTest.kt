package com.cocido.oficios.core.common.time

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayDateTimeTest {

    @Test
    fun `con Z convierte el instante a Buenos Aires`() {
        val zoned = DisplayDateTime.toZonedDateTime("2026-09-30T13:00:00.000Z")

        assertEquals(LocalDateTime.of(2026, 9, 30, 10, 0), zoned?.toLocalDateTime())
    }

    @Test
    fun `con offset convierte el instante a Buenos Aires`() {
        val zoned = DisplayDateTime.toZonedDateTime("2026-09-30T16:00:00+00:00")

        assertEquals(LocalDateTime.of(2026, 9, 30, 13, 0), zoned?.toLocalDateTime())
    }

    @Test
    fun `sin offset conserva la hora local`() {
        val zoned = DisplayDateTime.toZonedDateTime("2026-09-30T13:00:00")

        assertEquals(LocalDateTime.of(2026, 9, 30, 13, 0), zoned?.toLocalDateTime())
        assertFalse(DisplayDateTime.hasZoneDesignator("2026-09-30T13:00:00"))
        assertTrue(DisplayDateTime.hasZoneDesignator("2026-09-30T13:00:00.000Z"))
        assertTrue(DisplayDateTime.hasZoneDesignator("2026-09-30T13:00:00-03:00"))
    }
}
