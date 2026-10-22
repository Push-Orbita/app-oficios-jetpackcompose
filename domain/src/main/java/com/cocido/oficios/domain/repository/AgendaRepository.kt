package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AgendaSessionConfig
import com.cocido.oficios.domain.model.AgendaSessionsSnapshot
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotsSnapshot
import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private val EmptySlots: StateFlow<AgendaSlotsSnapshot?> =
    MutableStateFlow<AgendaSlotsSnapshot?>(null).asStateFlow()
private val EmptySessions: StateFlow<AgendaSessionsSnapshot?> =
    MutableStateFlow<AgendaSessionsSnapshot?>(null).asStateFlow()
private val EmptyAgendaChanged: SharedFlow<Unit> = MutableSharedFlow<Unit>().asSharedFlow()

interface AgendaRepository {
    val slots: StateFlow<AgendaSlotsSnapshot?> get() = EmptySlots
    val sessions: StateFlow<AgendaSessionsSnapshot?> get() = EmptySessions
    val agendaChanged: SharedFlow<Unit> get() = EmptyAgendaChanged
    suspend fun getAvailableSlots(
        subRubroId: Long,
        fromIso: String,
        toIso: String,
    ): AppResult<List<AgendaSlot>>
    suspend fun getSlotsForSubRubro(
        subRubroId: Long,
        fromIso: String? = null,
        toIso: String? = null,
    ): AppResult<List<AgendaSlot>>
    suspend fun createSlot(params: CreateAgendaSlotParams): AppResult<Long>
    suspend fun updateSlot(params: UpdateAgendaSlotParams): AppResult<Unit>
    suspend fun deleteSlot(slotId: Long): AppResult<Unit>
    suspend fun getSessionsForSubRubro(subRubroId: Long): AppResult<List<AgendaSessionConfig>>
    suspend fun saveSession(params: SaveAgendaSessionParams): AppResult<Unit>
    suspend fun deleteSession(sessionId: Long): AppResult<Unit>
}
