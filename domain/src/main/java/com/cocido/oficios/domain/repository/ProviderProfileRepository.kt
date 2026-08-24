package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.model.RegisterTradeDraft

interface ProviderProfileRepository {
    suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit>
    suspend fun submitRubroFull(request: ProviderRubroFullRequest): AppResult<Unit>
    suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest): AppResult<Unit>
    suspend fun finalizeProfile(): AppResult<Unit>
    suspend fun loadProviderProfileForSync(): AppResult<ProviderProfileSync>
    suspend fun getMySubRubros(): AppResult<List<ProviderSubRubroOption>>
}
