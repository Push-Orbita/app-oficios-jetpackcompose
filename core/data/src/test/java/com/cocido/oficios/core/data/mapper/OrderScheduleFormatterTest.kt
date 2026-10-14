package com.cocido.oficios.core.data.mapper

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderScheduleFormatterTest {

    private val argentina = ZoneId.of("America/Argentina/Buenos_Aires")

    @Test
    fun `un rango en UTC se muestra en hora de Argentina y no se invierte`() {
        val label = formatScheduledRange(
            startIso = "2026-10-13T19:54:00.000Z",
            endIso = "2026-10-13T21:54:00.000Z",
            zone = argentina,
        )

        assertEquals("13 oct, 16:54 - 18:54", label)
    }

    @Test
    fun `inicio con offset y fin en UTC usan el mismo huso`() {
        // 16:54 ART y 17:54Z (14:54 ART). Formatear cada uno con su offset
        // producía "13 oct, 16:54 - 17:54" o, ya convertido solo el fin, "16:54 - 14:54".
        val label = formatScheduledRange(
            startIso = "2026-10-13T16:54:00-03:00",
            endIso = "2026-10-13T17:54:00.000Z",
            zone = argentina,
        )

        assertEquals("13 oct, 14:54 - 16:54", label)
    }

    @Test
    fun `si el fin cae al dia siguiente conserva la fecha`() {
        val label = formatScheduledRange(
            startIso = "2026-10-13T16:54:00-03:00",
            endIso = "2026-10-14T14:54:00-03:00",
            zone = argentina,
        )

        assertEquals("13 oct, 16:54 - 14 oct, 14:54", label)
    }
}
