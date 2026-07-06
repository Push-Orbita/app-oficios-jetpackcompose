package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.auth.dto.LoginRequestDto
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenRequestDto
import com.cocido.oficios.core.network.api.auth.dto.RequestPasswordResetCodeDto
import com.cocido.oficios.core.network.api.auth.dto.ResetPasswordWithCodeDto
import com.cocido.oficios.core.network.api.auth.dto.RolUsuarioDto
import com.cocido.oficios.core.network.api.auth.dto.VerifyEmailRequestDto
import com.cocido.oficios.core.network.api.auth.dto.VerifyPasswordResetCodeDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import com.cocido.oficios.core.network.api.auth.AuthSignupRemoteDataSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi,
    private val authSignupRemoteDataSource: AuthSignupRemoteDataSource,
    private val json: Json,
) {

    suspend fun login(email: String, password: String) = safeApiCall {
        authApi.login(LoginRequestDto(email = email, contrasena = password))
    }

    suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        roleId: Long,
        document: String?,
    ) = safeApiCall {
        authSignupRemoteDataSource.signup(firstName, lastName, email, password, roleId, document)
    }

    suspend fun refreshToken(refreshToken: String) = safeApiCall {
        authApi.refreshToken(RefreshTokenRequestDto(refreshToken))
    }

    suspend fun getCurrentUser() = safeApiCall { authApi.getCurrentUser() }

    suspend fun logout() = safeApiCall { authApi.logout() }

    suspend fun requestPasswordReset(email: String) = safeApiCall {
        authApi.requestPasswordReset(RequestPasswordResetCodeDto(email))
    }

    suspend fun verifyPasswordResetCode(code: String): AppResult<String> = when (
        val result = safeApiCall { authApi.verifyPasswordResetCode(VerifyPasswordResetCodeDto(code)) }
    ) {
        is AppResult.Success -> {
            val token = result.data["reset_token"] ?: result.data["resetToken"]
            if (token.isNullOrBlank()) {
                AppResult.Error(com.cocido.oficios.core.common.result.AppError.Unknown("No se recibió token de recuperación"))
            } else {
                AppResult.Success(token)
            }
        }
        is AppResult.Error -> result
    }

    suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) = safeApiCall {
        authApi.resetPassword(
            authorization = "Bearer $resetToken",
            request = ResetPasswordWithCodeDto(
                contrasena = password,
                confirmarContrasena = confirmPassword,
            ),
        )
    }

    suspend fun sendVerificationEmail() = safeApiCall {
        authApi.sendVerificationEmail().message
    }

    suspend fun verifyEmail(code: String) = safeApiCall {
        authApi.verifyEmail(VerifyEmailRequestDto(codigo = code))
    }

    suspend fun getUserRoles() = safeApiCall {
        parseRoles(authApi.searchUserRoles())
    }

    suspend fun getClientProfileStatus() = safeApiCall { authApi.getClientProfileStatus() }

    suspend fun getClientProfile() = safeApiCall { authApi.getClientProfile() }

    suspend fun getProviderProfileStatus() = safeApiCall { authApi.getProviderProfileStatus() }

    suspend fun getProviderProfile() = safeApiCall { authApi.getProviderProfile() }

    private fun parseRoles(response: Map<String, kotlinx.serialization.json.JsonElement>): List<RolUsuarioDto> {
        val dataElement = response["data"] ?: response["items"] ?: return emptyList()
        return when (dataElement) {
            is JsonArray -> dataElement.map { json.decodeFromJsonElement<RolUsuarioDto>(it) }
            is JsonObject -> {
                val nested = dataElement["data"] ?: dataElement["items"]
                if (nested is JsonArray) {
                    nested.map { json.decodeFromJsonElement<RolUsuarioDto>(it) }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
