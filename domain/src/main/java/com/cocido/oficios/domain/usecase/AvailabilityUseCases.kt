package com.cocido.oficios.domain.usecase

import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.repository.AvailabilityRepository
import javax.inject.Inject

class AvailabilityUseCases @Inject constructor(
    private val repository: AvailabilityRepository,
) {
    suspend fun getWeekly(subRubroId: Long) = repository.getWeeklyAvailability(subRubroId)
    suspend fun replaceWeekly(subRubroId: Long, week: WeeklyAvailability) =
        repository.replaceWeeklyAvailability(subRubroId, week)
    suspend fun listBlocks(subRubroId: Long) = repository.listBlocks(subRubroId)
    suspend fun createBlock(params: CreateAgendaBlockParams) = repository.createBlock(params)
    suspend fun deleteBlock(subRubroId: Long, blockId: Long) =
        repository.deleteBlock(subRubroId, blockId)
}
