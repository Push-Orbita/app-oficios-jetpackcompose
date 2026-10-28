package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.agenda.dto.SlotTurnoAgendaDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaMapperTest {

    @Test
    fun blockedSlotWithZeroIdIsNotSelectable() {
        val slot = SlotTurnoAgendaDto(
            id = 0,
            inicio = "2026-10-26T10:00:00-03:00",
            fin = "2026-10-26T11:00:00-03:00",
            estado = "BLOQUEADO",
        ).toDomain()
        assertEquals(0L, slot.id)
        assertFalse(slot.isSelectable)
    }

    @Test
    fun availableSlotWithRealIdIsSelectable() {
        val slot = SlotTurnoAgendaDto(
            id = 31,
            inicio = "2026-10-26T10:00:00-03:00",
            fin = "2026-10-26T11:00:00-03:00",
            estado = "DISPONIBLE",
        ).toDomain()
        assertTrue(slot.isSelectable)
    }
}
