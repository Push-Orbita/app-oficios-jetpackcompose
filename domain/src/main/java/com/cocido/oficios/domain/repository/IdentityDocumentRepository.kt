package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderDniDocuments

interface IdentityDocumentRepository {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String): AppResult<Unit>
    suspend fun getMyDniDocuments(): AppResult<ProviderDniDocuments?>
}
