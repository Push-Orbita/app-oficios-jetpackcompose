package com.cocido.oficios.core.network.api.safety

import com.cocido.oficios.core.network.api.safety.dto.CreateDenunciaRequestDto
import com.cocido.oficios.core.network.api.safety.dto.DenunciaDto
import com.cocido.oficios.core.network.api.safety.dto.DenunciaMotivosResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ReportsApi {

    @GET("denuncias/motivos")
    suspend fun getMotivos(): DenunciaMotivosResponseDto

    @POST("denuncias")
    suspend fun create(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateDenunciaRequestDto,
    ): DenunciaDto
}
