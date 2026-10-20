package com.cocido.oficios.feature.home.agenda

import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgendaListUiStateTest {

    private val existingSlot = AgendaSlot(
        id = 10L,
        subRubroId = 2L,
        subRubroLabel = "Peluquería canina",
        start = LocalDateTime.of(2030, 3, 12, 10, 0),
        end = LocalDateTime.of(2030, 3, 12, 11, 0),
        status = AgendaSlotStatus.DISPONIBLE,
        autoAcceptance = false,
    )

    @Test
    fun `un nuevo horario superpuesto queda bloqueado`() {
        val state = AgendaListUiState.Success(
            slots = listOf(existingSlot),
            slotStart = LocalDateTime.of(2030, 3, 12, 10, 30),
            slotEnd = LocalDateTime.of(2030, 3, 12, 11, 30),
        )

        assertEquals(AgendaSlotFormIssue.OVERLAP, state.derivedFormIssue)
    }

    @Test
    fun `dos horarios contiguos no se consideran superpuestos`() {
        val state = AgendaListUiState.Success(
            slots = listOf(existingSlot),
            slotStart = LocalDateTime.of(2030, 3, 12, 11, 0),
            slotEnd = LocalDateTime.of(2030, 3, 12, 12, 0),
        )

        assertNull(state.derivedFormIssue)
    }

    @Test
    fun `editar un horario no se superpone consigo mismo`() {
        val state = AgendaListUiState.Success(
            slots = listOf(existingSlot),
            editingSlotId = existingSlot.id,
            slotStart = existingSlot.start,
            slotEnd = existingSlot.end,
        )

        assertNull(state.derivedFormIssue)
    }

    @Test
    fun `un horario que cruza de dia queda bloqueado`() {
        val state = AgendaListUiState.Success(
            slotStart = LocalDateTime.of(2030, 3, 12, 23, 30),
            slotEnd = LocalDateTime.of(2030, 3, 13, 0, 30),
        )

        assertEquals(AgendaSlotFormIssue.DIFFERENT_DAY, state.derivedFormIssue)
    }
}
