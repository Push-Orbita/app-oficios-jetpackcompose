package com.cocido.oficios.domain.usecase.auth

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
import com.cocido.oficios.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifyEmailUseCaseTest {

    @Test
    fun `verify success refreshes cached user`() = runTest {
        val verifiedUser = User(1, "user@test.com", "Juan", "Pérez", UserRole.CLIENT, true)
        var cachedUser: User? = null
        val useCase = VerifyEmailUseCase(
            authRepository = object : AuthRepository {
                override suspend fun login(email: String, password: String) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun registerClient(request: RegisterClientRequest) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun registerProvider(request: RegisterProviderRequest) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown())
                override suspend fun getCurrentUser() = AppResult.Success(verifiedUser)
                override suspend fun logout() = AppResult.Success(Unit)
                override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
                override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
                override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun sendVerificationEmail() = AppResult.Success("Código de verificación reenviado a tu correo")
                override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
                override suspend fun getUserRoles(): AppResult<List<UserRoleDefinition>> =
                    AppResult.Success(emptyList())
            },
            sessionRepository = object : SessionRepository {
                override val authState = MutableStateFlow(com.cocido.oficios.domain.model.AuthState.Unauthenticated)
                override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
                override suspend fun clearSession() = Unit
                override suspend fun getAccessToken() = "token"
                override suspend fun getRefreshToken() = "refresh"
                override suspend fun updateTokens(tokens: AuthTokens) = Unit
                override suspend fun getCachedUser() = null
                override suspend fun updateCachedUser(user: User) {
                    cachedUser = user
                }
            },
        )

        val result = useCase("123456")

        assertTrue(result is AppResult.Success)
        assertEquals(verifiedUser, cachedUser)
    }

    @Test
    fun `verify failure does not refresh user`() = runTest {
        var updateCalled = false
        val useCase = VerifyEmailUseCase(
            authRepository = object : AuthRepository {
                override suspend fun login(email: String, password: String) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun registerClient(request: RegisterClientRequest) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun registerProvider(request: RegisterProviderRequest) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown())
                override suspend fun getCurrentUser() = AppResult.Error(AppError.Unknown())
                override suspend fun logout() = AppResult.Success(Unit)
                override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
                override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
                override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
                    AppResult.Error(AppError.Unknown())
                override suspend fun sendVerificationEmail() = AppResult.Success("Código de verificación reenviado a tu correo")
                override suspend fun verifyEmail(code: String) =
                    AppResult.Error(AppError.Validation("Código inválido"))
                override suspend fun getUserRoles(): AppResult<List<UserRoleDefinition>> =
                    AppResult.Success(emptyList())
            },
            sessionRepository = object : SessionRepository {
                override val authState = MutableStateFlow(com.cocido.oficios.domain.model.AuthState.Unauthenticated)
                override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
                override suspend fun clearSession() = Unit
                override suspend fun getAccessToken() = null
                override suspend fun getRefreshToken() = null
                override suspend fun updateTokens(tokens: AuthTokens) = Unit
                override suspend fun getCachedUser() = null
                override suspend fun updateCachedUser(user: User) {
                    updateCalled = true
                }
            },
        )

        val result = useCase("000000")

        assertTrue(result is AppResult.Error)
        assertEquals(false, updateCalled)
    }
}
