package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRoleDefinition
import com.cocido.oficios.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<UserSession>
    suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession>
    suspend fun registerProvider(request: RegisterProviderRequest): AppResult<UserSession>
    suspend fun refreshToken(refreshToken: String): AppResult<AuthTokens>
    suspend fun getCurrentUser(): AppResult<User>
    suspend fun logout(): AppResult<Unit>
    suspend fun requestPasswordReset(email: String): AppResult<Unit>
    suspend fun verifyPasswordResetCode(code: String): AppResult<String>
    suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String): AppResult<Unit>
    suspend fun sendVerificationEmail(): AppResult<String>
    suspend fun verifyEmail(code: String): AppResult<Unit>
    suspend fun getUserRoles(): AppResult<List<UserRoleDefinition>>
}
