package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateContactoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.EnviarCodigoTelefonoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisContactosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.MisDomiciliosResponseDto
import com.cocido.oficios.core.network.api.profile.dto.VerifyTelefonoCodigoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.VerificarTelefonoCodigoResponseDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateFavoriteRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ClientProfileApi {

    @GET("domicilios/mis")
    suspend fun getMyDomicilios(): MisDomiciliosResponseDto

    @GET("contacto/mis")
    suspend fun getMyContactos(): MisContactosResponseDto

    /** OpenAPI 201 JSON; el body no se usa — Unit tolera texto plano o vacío. */
    @POST("domicilios")
    suspend fun createDomicilio(
        @Body request: CreateDomicilioRequestDto,
    )

    @POST("domicilios/mi")
    suspend fun createMyDomicilio(
        @Body request: UpdateDomicilioRequestDto,
    )

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

    @PATCH("domicilios/{id}")
    suspend fun updateDomicilio(
        @Path("id") id: Long,
        @Body request: UpdateDomicilioRequestDto,
    )

    /** OpenAPI 200 sin content; prod a veces manda text/plain. */
    @DELETE("domicilios/{id}")
    suspend fun deleteDomicilio(@Path("id") id: Long)

    @PATCH("domicilios/{id}/favorito")
    suspend fun setFavoriteDomicilio(
        @Path("id") id: Long,
        @Body request: UpdateFavoriteRequestDto,
    )

    @PATCH("contacto/{id}")
    suspend fun updateContacto(
        @Path("id") id: Long,
        @Body request: UpdateContactoRequestDto,
    )

    /** OpenAPI 200 sin content; prod: "Contacto eliminado correctamente". */
    @DELETE("contacto/{id}")
    suspend fun deleteContacto(@Path("id") id: Long)

    @PATCH("contacto/{id}/favorito")
    suspend fun setFavoriteContacto(
        @Path("id") id: Long,
        @Body request: UpdateFavoriteRequestDto,
    )
}
