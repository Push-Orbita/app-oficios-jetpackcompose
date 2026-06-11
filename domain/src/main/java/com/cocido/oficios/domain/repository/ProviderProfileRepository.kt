package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest

interface ProviderProfileRepository {
    suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit>
    suspend fun submitRubroFull(request: ProviderRubroFullRequest): AppResult<Unit>
    suspend fun finalizeProfile(): AppResult<Unit>
}
