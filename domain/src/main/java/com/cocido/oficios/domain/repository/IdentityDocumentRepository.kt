package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult

interface IdentityDocumentRepository {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String): AppResult<Unit>
}
