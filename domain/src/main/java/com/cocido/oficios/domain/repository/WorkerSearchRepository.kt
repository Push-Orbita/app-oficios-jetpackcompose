package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.PagedWorkers
import com.cocido.oficios.domain.model.WorkerSearchFilters

interface WorkerSearchRepository {
    suspend fun searchWorkers(
        filters: WorkerSearchFilters,
        pageNumber: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): AppResult<PagedWorkers>

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
