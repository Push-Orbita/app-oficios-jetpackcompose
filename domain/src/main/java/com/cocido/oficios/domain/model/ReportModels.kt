package com.cocido.oficios.domain.model

data class ReportReason(
    val code: String,
    val label: String,
)

data class SubmittedReport(
    val id: Long,
    val status: String,
)
