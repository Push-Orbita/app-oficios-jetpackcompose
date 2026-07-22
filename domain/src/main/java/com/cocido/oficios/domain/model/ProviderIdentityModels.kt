package com.cocido.oficios.domain.model

data class ProviderDniDocuments(
    val frontUrl: String?,
    val backUrl: String?,
    val frontStatus: String?,
    val backStatus: String?,
    val frontFileName: String?,
    val backFileName: String?,
)

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
