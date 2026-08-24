package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.SaveAgendaSessionParams

interface AgendaRepository {
    suspend fun getSlotsForSubRubro(subRubroId: Long): AppResult<List<AgendaSlot>>
    suspend fun createSlot(params: CreateAgendaSlotParams): AppResult<Long>
    suspend fun deleteSlot(slotId: Long): AppResult<Unit>
    suspend fun getSessionsForSubRubro(subRubroId: Long): AppResult<List<AgendaSessionConfig>>
    suspend fun saveSession(params: SaveAgendaSessionParams): AppResult<Unit>
    suspend fun deleteSession(sessionId: Long): AppResult<Unit>
}
