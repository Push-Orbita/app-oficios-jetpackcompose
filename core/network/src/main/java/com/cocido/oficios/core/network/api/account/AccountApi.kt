package com.cocido.oficios.core.network.api.account

import com.cocido.oficios.core.network.api.account.dto.AccountDeletionRequestDto
import com.cocido.oficios.core.network.api.account.dto.ChangePasswordRequestDto
import com.cocido.oficios.core.network.api.account.dto.CreateAccountDeletionRequestDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import kotlinx.serialization.json.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AccountApi {

    @GET("auth/me")
    suspend fun getCurrentUser(): UsuarioDto

    @Multipart
    @PATCH("usuario/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Part("nombres") firstName: RequestBody,
        @Part("apellido") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part profilePhoto: MultipartBody.Part? = null,
        @Part("urlFotoPerfil") currentPhotoUrl: RequestBody? = null,
    ): UsuarioDto

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDto): JsonElement

    @POST("usuario/me/deletion-requests")
    suspend fun createDeletionRequest(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateAccountDeletionRequestDto,
    ): AccountDeletionRequestDto

    @GET("usuario/me/deletion-requests/current")
    suspend fun getCurrentDeletionRequest(): AccountDeletionRequestDto

    @DELETE("usuario/me/deletion-requests/current")
    suspend fun cancelCurrentDeletionRequest(): AccountDeletionRequestDto
}
