package com.cocido.oficios.domain.usecase

import com.cocido.oficios.domain.model.CreateAgendaSlotParams
import com.cocido.oficios.domain.model.SaveAgendaSessionParams
import com.cocido.oficios.domain.model.UpdateAgendaSlotParams
import com.cocido.oficios.domain.repository.AgendaRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import javax.inject.Inject

class AgendaUseCases @Inject constructor(
    private val repository: AgendaRepository,
) {
    suspend fun getSlotsForSubRubro(subRubroId: Long, fromIso: String? = null, toIso: String? = null) =
        repository.getSlotsForSubRubro(subRubroId, fromIso, toIso)
    suspend fun createSlot(params: CreateAgendaSlotParams) = repository.createSlot(params)
    suspend fun updateSlot(params: UpdateAgendaSlotParams) = repository.updateSlot(params)
    suspend fun deleteSlot(slotId: Long) = repository.deleteSlot(slotId)
    suspend fun getSessionsForSubRubro(subRubroId: Long) = repository.getSessionsForSubRubro(subRubroId)
    suspend fun saveSession(params: SaveAgendaSessionParams) = repository.saveSession(params)
    suspend fun deleteSession(sessionId: Long) = repository.deleteSession(sessionId)
}

class GetMySubRubrosUseCase @Inject constructor(
    private val repository: ProviderProfileRepository,
) {
    suspend operator fun invoke() = repository.getMySubRubros()
}
