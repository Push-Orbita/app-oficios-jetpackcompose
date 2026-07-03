package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorPerfilEstadoDto
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ProviderProfileApi {

    @PATCH("trabajador-oficio/me")
    suspend fun updateProfile(
        @Body request: UpdateTrabajadorPerfilProfesionalRequestDto,
    ): TrabajadorOficioPerfilMeDto

    @POST("trabajador-oficio-rubro/full")
    suspend fun createRubroFull(
        @Body request: CreateFullAltaRubrosSubrubrosRequestDto,
    ): JsonElement

    @POST("trabajador-oficio-rubro/full/secundario")
    suspend fun createSecondaryRubroFull(
        @Body request: CreateFullAltaRubrosSubrubrosRequestDto,
    ): JsonElement

    @POST("trabajador-oficio/me/finalizar-perfil")
    suspend fun finalizeProfile(): TrabajadorPerfilEstadoDto
}
