package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderSelfieDocument

interface IdentityDocumentRepository {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String): AppResult<Unit>
    suspend fun uploadSelfie(imagePath: String): AppResult<Unit>
    suspend fun replaceSelfie(documentId: Long, imagePath: String): AppResult<Unit>
    suspend fun getMyDniDocuments(): AppResult<ProviderDniDocuments?>
    suspend fun getMySelfieDocument(): AppResult<ProviderSelfieDocument?>
}
