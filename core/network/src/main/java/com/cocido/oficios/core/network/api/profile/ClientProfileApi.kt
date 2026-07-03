package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.MisContactosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisDomiciliosResponseDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ClientProfileApi {

    @GET("domicilios/mis")
    suspend fun getMyDomicilios(): MisDomiciliosResponseDto

    @GET("contacto/mis")
    suspend fun getMyContactos(): MisContactosResponseDto

    @POST("domicilios")
    suspend fun createDomicilio(
        @Body request: CreateDomicilioRequestDto,
    ): JsonElement

    @POST("contacto")
    suspend fun createContacto(
        @Body request: CreateContactoRequestDto,
    ): JsonElement
}
