package com.cocido.oficios.feature.auth

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository

class FakeAuthRepository(
    var loginResult: AppResult<UserSession> = AppResult.Success(
        UserSession(
            tokens = AuthTokens("access", "refresh"),
            user = User(1, "user@test.com", "Juan", "Pérez", UserRole.CLIENT, true),
        ),
    ),
    var registerClientResult: AppResult<UserSession> = loginResult,
    var registerProviderResult: AppResult<UserSession> = loginResult,
    var lastClientRequest: RegisterClientRequest? = null,
    var lastProviderRequest: RegisterProviderRequest? = null,
) : AuthRepository {
    override suspend fun login(email: String, password: String) = loginResult
    override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> {
        lastClientRequest = request
        return registerClientResult
    }
    override suspend fun registerProvider(request: RegisterProviderRequest): AppResult<UserSession> {
        lastProviderRequest = request
        return registerProviderResult
    }
    override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown())
    override suspend fun getCurrentUser() = AppResult.Error(AppError.Unknown())
    override suspend fun logout() = AppResult.Success(Unit)
    override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
    override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
    override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
        AppResult.Error(AppError.Unknown())
    override suspend fun sendVerificationEmail() = AppResult.Success("Código de verificación reenviado a su correo")
    override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
    override suspend fun getUserRoles() = AppResult.Success(
        listOf(
            UserRoleDefinition(1, "Cliente", UserRole.CLIENT),
            UserRoleDefinition(2, "Trabajador", UserRole.PROVIDER),
        ),
    )
}
