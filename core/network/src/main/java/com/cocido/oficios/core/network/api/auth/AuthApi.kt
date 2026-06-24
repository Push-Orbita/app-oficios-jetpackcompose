package com.cocido.oficios.core.network.api.auth

import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilEstadoDto
import com.cocido.oficios.core.network.api.auth.dto.ClientePerfilMeDto
import com.cocido.oficios.core.network.api.auth.dto.LoginRequestDto
import com.cocido.oficios.core.network.api.auth.dto.MessageResponseDto
import com.cocido.oficios.core.network.api.auth.dto.LoginResponseDto
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenRequestDto
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenResponseDto
import com.cocido.oficios.core.network.api.auth.dto.RequestPasswordResetCodeDto
import com.cocido.oficios.core.network.api.auth.dto.ResetPasswordWithCodeDto
import com.cocido.oficios.core.network.api.auth.dto.RolUsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.SignupResponseDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorOficioPerfilMeDto
import com.cocido.oficios.core.network.api.auth.dto.TrabajadorPerfilEstadoDto
import com.cocido.oficios.core.network.api.auth.dto.UsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.VerifyEmailRequestDto
import com.cocido.oficios.core.network.api.auth.dto.VerifyPasswordResetCodeDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @Multipart
    @POST("auth/signup")
    suspend fun signup(
        @Part("nombres") firstName: RequestBody,
        @Part("apellido") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("contrasena") password: RequestBody,
        @Part("rolId") roleId: RequestBody,
        @Part("documento") document: RequestBody? = null,
    ): SignupResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): RefreshTokenResponseDto

    @GET("auth/me")
    suspend fun getCurrentUser(): UsuarioDto

    @POST("auth/logout")
    suspend fun logout()

    @POST("auth/password-reset/request-code")
    suspend fun requestPasswordReset(@Body request: RequestPasswordResetCodeDto)

    @POST("auth/password-reset/verify-code")
    suspend fun verifyPasswordResetCode(@Body request: VerifyPasswordResetCodeDto): Map<String, String>

    @POST("auth/password-reset/confirm")
    suspend fun resetPassword(
        @Header("Authorization") authorization: String,
        @Body request: ResetPasswordWithCodeDto,
    )

    @POST("auth/send-verification-email")
    suspend fun sendVerificationEmail(): MessageResponseDto

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto)

    @GET("rol-usuario/search")
    suspend fun searchUserRoles(
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageNumber") pageNumber: Int = 1,
    ): Map<String, kotlinx.serialization.json.JsonElement>

    @GET("cliente/me/estado-perfil")
    suspend fun getClientProfileStatus(): ClientePerfilEstadoDto

    @GET("cliente/me")
    suspend fun getClientProfile(): ClientePerfilMeDto

    @GET("trabajador-oficio/me/estado-perfil")
    suspend fun getProviderProfileStatus(): TrabajadorPerfilEstadoDto

    @GET("trabajador-oficio/me")
    suspend fun getProviderProfile(): TrabajadorOficioPerfilMeDto
}
