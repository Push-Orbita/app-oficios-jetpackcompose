package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderProfileRemoteDataSource @Inject constructor(
    private val providerProfileApi: ProviderProfileApi,
) {
    suspend fun updateProfessionalInfo(request: UpdateTrabajadorPerfilProfesionalRequestDto) = safeApiCall {
        providerProfileApi.updateProfile(request)
    }

    suspend fun createRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) = safeApiCall {
        providerProfileApi.createRubroFull(request)
    }

    suspend fun finalizeProfile() = safeApiCall {
        providerProfileApi.finalizeProfile()
    }
}
