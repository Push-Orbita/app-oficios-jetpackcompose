package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ReportReason
import com.cocido.oficios.domain.model.SubmittedReport

interface ReportsRepository {
    suspend fun getReasons(): AppResult<List<ReportReason>>
    suspend fun submit(
        workerId: Long,
        reasonCode: String,
        description: String,
        idempotencyKey: String,
        bookingId: Long? = null,
    ): AppResult<SubmittedReport>
}
