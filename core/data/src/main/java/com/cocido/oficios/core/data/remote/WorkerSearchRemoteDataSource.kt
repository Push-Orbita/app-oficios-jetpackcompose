package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.search.WorkerSearchApi
import com.cocido.oficios.core.network.api.search.dto.WorkerSearchItemDto
import com.cocido.oficios.domain.model.AttentionModality
import com.cocido.oficios.domain.model.WorkerSearchFilters
import javax.inject.Inject

class WorkerSearchRemoteDataSource @Inject constructor(
    private val workerSearchApi: WorkerSearchApi,
) {
    suspend fun searchWorkers(
        filters: WorkerSearchFilters,
        pageNumber: Int,
        pageSize: Int,
    ): PaginatedResponseDto<WorkerSearchItemDto> =
        workerSearchApi.searchWorkers(
            rubroId = filters.rubroId,
            subRubroId = filters.subRubroId,
            modalidadAtencion = filters.modalidadAtencion?.name,
            localidadId = filters.localidadId,
            barrioId = filters.barrioId,
            fechaDeseada = filters.fechaDeseada,
            query = filters.query?.takeIf { it.isNotBlank() },
            pageNumber = pageNumber,
            pageSize = pageSize,
        )
}
