package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.WorkCertificateInfo

interface WorkCertificateRepository {
    suspend fun uploadCertificate(name: String, filePath: String): AppResult<WorkCertificateInfo>
    suspend fun getMyCertificates(): AppResult<List<WorkCertificateInfo>>
}
