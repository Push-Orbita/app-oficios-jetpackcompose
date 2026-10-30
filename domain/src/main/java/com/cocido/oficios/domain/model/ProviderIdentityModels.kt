package com.cocido.oficios.domain.model

data class ProviderDniDocuments(
    val id: Long?,
    val frontUrl: String?,
    val backUrl: String?,
    val frontStatus: String?,
    val backStatus: String?,
    val frontFileName: String?,
    val backFileName: String?,
) {
    val isApproved: Boolean
        get() = frontStatus?.equals("APROBADO", ignoreCase = true) == true &&
            backStatus?.equals("APROBADO", ignoreCase = true) == true

    val isRejected: Boolean
        get() = frontStatus?.equals("RECHAZADO", ignoreCase = true) == true ||
            backStatus?.equals("RECHAZADO", ignoreCase = true) == true

    /** Pendiente de admin (al menos un lado SUBIDO y ninguno rechazado). */
    val isPendingReview: Boolean
        get() = !isApproved && !isRejected && (
            frontStatus?.equals("SUBIDO", ignoreCase = true) == true ||
                backStatus?.equals("SUBIDO", ignoreCase = true) == true
            )
}

data class ProviderSelfieDocument(
    val id: Long?,
    val url: String?,
    val status: String?,
    val rejectionReason: String?,
)

data class WorkCertificateInfo(
    val id: Long,
    val name: String,
    val fileUrl: String?,
)
