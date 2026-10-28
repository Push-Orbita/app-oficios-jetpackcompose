package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AgendaBlock
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.DayAvailabilityWindow
import com.cocido.oficios.domain.model.WeeklyAvailability

interface AvailabilityRepository {
    suspend fun getWeeklyAvailability(subRubroId: Long): AppResult<WeeklyAvailability>
    suspend fun replaceWeeklyAvailability(
        subRubroId: Long,
        week: WeeklyAvailability,
    ): AppResult<WeeklyAvailability>
    suspend fun listBlocks(subRubroId: Long): AppResult<List<AgendaBlock>>
    suspend fun createBlock(params: CreateAgendaBlockParams): AppResult<AgendaBlock>
    suspend fun deleteBlock(subRubroId: Long, blockId: Long): AppResult<Unit>
    suspend fun getDayAvailability(
        workerId: Long,
        date: String,
        subRubroId: Long,
    ): AppResult<DayAvailabilityWindow>
}
