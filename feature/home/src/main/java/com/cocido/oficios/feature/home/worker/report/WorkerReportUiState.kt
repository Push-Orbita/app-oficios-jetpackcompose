package com.cocido.oficios.feature.home.worker.report

import com.cocido.oficios.domain.model.ReportReason
import com.cocido.oficios.domain.usecase.safety.SubmitReportUseCase

data class WorkerReportUiState(
    val workerName: String = "",
    val isLoadingReasons: Boolean = true,
    val reasons: List<ReportReason> = emptyList(),
    val selectedReasonCode: String? = null,
    val description: String = "",
    val errorMessage: String? = null,
    val reasonsErrorMessage: String? = null,
    val isSubmitting: Boolean = false,
) {
    val selectedReasonLabel: String
        get() = reasons.firstOrNull { it.code == selectedReasonCode }?.label.orEmpty()

    val descriptionHint: String
        get() {
            val length = description.trim().length
            return when {
                length == 0 -> "Mínimo ${SubmitReportUseCase.MIN_DESCRIPTION_LENGTH} caracteres"
                length < SubmitReportUseCase.MIN_DESCRIPTION_LENGTH ->
                    "Faltan ${SubmitReportUseCase.MIN_DESCRIPTION_LENGTH - length} caracteres"
                else -> "$length / ${SubmitReportUseCase.MAX_DESCRIPTION_LENGTH}"
            }
        }

    val canSubmit: Boolean
        get() = !isSubmitting &&
            selectedReasonCode != null &&
            description.trim().length in SubmitReportUseCase.MIN_DESCRIPTION_LENGTH..SubmitReportUseCase.MAX_DESCRIPTION_LENGTH
}

sealed interface WorkerReportEvent {
    data object Retry : WorkerReportEvent
    data class ReasonSelected(val label: String) : WorkerReportEvent
    data class DescriptionChanged(val value: String) : WorkerReportEvent
    data object Submit : WorkerReportEvent
}

sealed interface WorkerReportEffect {
    data object NavigateBack : WorkerReportEffect
    data class ShowMessage(val message: String) : WorkerReportEffect
}
