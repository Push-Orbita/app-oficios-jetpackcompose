package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateTrabajadorOficioSubRubroRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorOficioSubRubroWorkerDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderProfileRemoteDataSource @Inject constructor(
    private val providerProfileApi: ProviderProfileApi,
    private val authApi: AuthApi,
) {
    suspend fun updateProfessionalInfo(request: UpdateTrabajadorPerfilProfesionalRequestDto) = safeApiCall {
        providerProfileApi.updateProfile(request)
    }

    suspend fun createRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) = safeApiCall {
        providerProfileApi.createRubroFull(request)
    }

    suspend fun createSecondaryRubroFull(request: CreateFullAltaRubrosSubrubrosRequestDto) = safeApiCall {
        providerProfileApi.createSecondaryRubroFull(request)
    }

    suspend fun finalizeProfile() = safeApiCall {
        providerProfileApi.finalizeProfile()
    }

    suspend fun getProviderProfile() = safeApiCall {
        authApi.getProviderProfile()
    }

    suspend fun deleteRubro(id: Long) = safeApiCall {
        providerProfileApi.deleteRubro(id)
    }

    suspend fun createSubRubro(request: CreateTrabajadorOficioSubRubroRequestDto) = safeApiCall {
        providerProfileApi.createSubRubro(request)
    }

    suspend fun updateSubRubro(id: Long, request: UpdateTrabajadorOficioSubRubroWorkerDto) = safeApiCall {
        providerProfileApi.updateSubRubro(id, request)
    }

    suspend fun deleteSubRubro(id: Long) = safeApiCall {
        providerProfileApi.deleteSubRubro(id)
    }
}
