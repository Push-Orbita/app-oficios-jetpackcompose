package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateContactoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.EnviarCodigoTelefonoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisContactosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisDomiciliosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.VerifyTelefonoCodigoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.VerificarTelefonoCodigoResponseDto
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
    ): CreateContactoResponseDto

    @POST("contacto/{id}/enviar-codigo-telefono")
    suspend fun sendPhoneVerificationCode(
        @Path("id") contactId: Long,
    ): EnviarCodigoTelefonoResponseDto

    @POST("contacto/{id}/verificar-codigo-telefono")
    suspend fun verifyPhoneCode(
        @Path("id") contactId: Long,
        @Body request: VerifyTelefonoCodigoRequestDto,
    ): VerificarTelefonoCodigoResponseDto
}
