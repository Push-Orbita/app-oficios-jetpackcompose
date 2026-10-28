package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toDto
import com.cocido.oficios.core.data.mapper.toReplaceDto
import com.cocido.oficios.core.data.remote.AvailabilityRemoteDataSource
import com.cocido.oficios.domain.model.CreateAgendaBlockParams
import com.cocido.oficios.domain.model.WeeklyAvailability
import com.cocido.oficios.domain.repository.AvailabilityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class AvailabilityRepositoryImpl @Inject constructor(
    private val remote: AvailabilityRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : AvailabilityRepository {

    override suspend fun getWeeklyAvailability(subRubroId: Long) =
        withContext(dispatcherProvider.io) {
            remote.getWeekly(subRubroId).map { it.toDomain() }
        }

    override suspend fun replaceWeeklyAvailability(
        subRubroId: Long,
        week: WeeklyAvailability,
    ) = withContext(dispatcherProvider.io) {
        remote.replaceWeekly(subRubroId, week.toReplaceDto()).map { it.toDomain() }
    }

    override suspend fun listBlocks(subRubroId: Long) =
        withContext(dispatcherProvider.io) {
            remote.listBlocks(subRubroId).map { blocks -> blocks.map { it.toDomain() } }
        }

    override suspend fun createBlock(params: CreateAgendaBlockParams) =
        withContext(dispatcherProvider.io) {
            remote.createBlock(params.subRubroId, params.toDto()).map { it.toDomain() }
        }

    override suspend fun deleteBlock(subRubroId: Long, blockId: Long) =
        withContext(dispatcherProvider.io) {
            remote.deleteBlock(subRubroId, blockId)
        }

    override suspend fun getDayAvailability(
        workerId: Long,
        date: String,
        subRubroId: Long,
    ) = withContext(dispatcherProvider.io) {
        remote.getDayAvailability(workerId, date, subRubroId).map { it.toDomain() }
    }
}
