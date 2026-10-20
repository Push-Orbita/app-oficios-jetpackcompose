package com.cocido.oficios.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleClockTest {

    @Test
    fun `el horario de servicios no muestra segundos`() {
        assertEquals("09:00", scheduleClock("09:00:00"))
        assertEquals("18:00", scheduleClock("18:00:00.000"))
        assertEquals("09:00", scheduleClock("9:00"))
    }
}
