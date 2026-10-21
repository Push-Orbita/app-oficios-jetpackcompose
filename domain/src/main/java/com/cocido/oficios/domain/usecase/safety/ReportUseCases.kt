package com.cocido.oficios.domain.usecase.safety

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ReportReason
import com.cocido.oficios.domain.model.SubmittedReport
import com.cocido.oficios.domain.repository.ReportsRepository
import javax.inject.Inject

class GetReportReasonsUseCase @Inject constructor(
    private val reportsRepository: ReportsRepository,
) {
    suspend operator fun invoke(): AppResult<List<ReportReason>> = reportsRepository.getReasons()
}

class SubmitReportUseCase @Inject constructor(
    private val reportsRepository: ReportsRepository,
) {
    suspend operator fun invoke(
        workerId: Long,
        reasonCode: String,
        description: String,
        idempotencyKey: String,
        bookingId: Long? = null,
    ): AppResult<SubmittedReport> {
        val trimmed = description.trim()
        if (reasonCode.isBlank()) {
            return AppResult.Error(
                AppError.Validation(
                    message = "Seleccione un motivo de la lista.",
                    backendCode = "REPORT_REASON_INVALID",
                ),
            )
        }
        if (trimmed.length < MIN_DESCRIPTION_LENGTH) {
            return AppResult.Error(
                AppError.Validation(
                    message = "Cuéntenos qué pasó con un poco más de detalle (mínimo $MIN_DESCRIPTION_LENGTH caracteres).",
                    backendCode = "REPORT_DESCRIPTION_INVALID",
                ),
            )
        }
        if (trimmed.length > MAX_DESCRIPTION_LENGTH) {
            return AppResult.Error(
                AppError.Validation(
                    message = "La descripción es demasiado larga (máximo $MAX_DESCRIPTION_LENGTH caracteres).",
                    backendCode = "REPORT_DESCRIPTION_INVALID",
                ),
            )
        }
        return reportsRepository.submit(
            workerId = workerId,
            reasonCode = reasonCode,
            description = trimmed,
            idempotencyKey = idempotencyKey,
            bookingId = bookingId,
        )
    }

    companion object {
        const val MIN_DESCRIPTION_LENGTH = 20
        const val MAX_DESCRIPTION_LENGTH = 2000
    }
}
