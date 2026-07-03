package com.cocido.oficios.domain.usecase

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.SessionRepository
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveInitialDestinationUseCaseTest {

    private val clientUser = User(1, "c@test.com", "Ana", "López", UserRole.CLIENT, true)
    private val providerUser = User(2, "p@test.com", "Pedro", "Gómez", UserRole.PROVIDER, true)

    @Test
    fun `no session goes to login`() = runTest {
        val useCase = createUseCase(accessToken = null)
        val result = useCase()
        assertEquals(InitialDestination.Login, (result as AppResult.Success).data)
    }

    @Test
    fun `incomplete client goes to onboarding`() = runTest {
        val useCase = createUseCase(
            accessToken = "token",
            user = clientUser,
            clientStatus = ClientProfileStatus(
                isComplete = false,
                missingSteps = listOf("domicilio"),
                hasContact = false,
                hasAddress = false,
                emailVerified = false,
                accountStatus = AccountStatus.ACTIVE,
            ),
        )
        val result = useCase()
        assertEquals(InitialDestination.ClientOnboarding, (result as AppResult.Success).data)
    }

    @Test
    fun `active client goes to home`() = runTest {
        val useCase = createUseCase(
            accessToken = "token",
            user = clientUser,
            clientStatus = ClientProfileStatus(
                isComplete = true,
                missingSteps = emptyList(),
                hasContact = true,
                hasAddress = true,
                emailVerified = true,
                accountStatus = AccountStatus.ACTIVE,
            ),
        )
        val result = useCase()
        assertEquals(InitialDestination.ClientHome, (result as AppResult.Success).data)
    }

    @Test
    fun `provider pending approval`() = runTest {
        val useCase = createUseCase(
            accessToken = "token",
            user = providerUser,
            providerStatus = ProviderProfileStatus(
                accountStatus = AccountStatus.PENDING_APPROVAL,
                isConfigurationComplete = true,
                isProfileComplete = false,
                isOperational = false,
                missingSteps = emptyList(),
            ),
        )
        val result = useCase()
        assertEquals(InitialDestination.ProviderPendingApproval, (result as AppResult.Success).data)
    }

    @Test
    fun `approved provider goes to home`() = runTest {
        val useCase = createUseCase(
            accessToken = "token",
            user = providerUser,
            providerStatus = ProviderProfileStatus(
                accountStatus = AccountStatus.APPROVED,
                isConfigurationComplete = true,
                isProfileComplete = true,
                isOperational = true,
                missingSteps = emptyList(),
            ),
        )
        val result = useCase()
        assertEquals(InitialDestination.ProviderHome, (result as AppResult.Success).data)
    }

    @Test
    fun `invalid token goes to login`() = runTest {
        val useCase = createUseCase(
            accessToken = "token",
            userResult = AppResult.Error(AppError.Unauthorized("expired")),
        )
        val result = useCase()
        assertEquals(InitialDestination.Login, (result as AppResult.Success).data)
    }

    private fun createUseCase(
        accessToken: String?,
        user: User = clientUser,
        userResult: AppResult<User> = AppResult.Success(user),
        clientStatus: ClientProfileStatus = ClientProfileStatus(
            isComplete = false,
            missingSteps = emptyList(),
            hasContact = false,
            hasAddress = false,
            emailVerified = false,
            accountStatus = AccountStatus.ACTIVE,
        ),
        providerStatus: ProviderProfileStatus = ProviderProfileStatus(
            accountStatus = AccountStatus.INCOMPLETE,
            isConfigurationComplete = false,
            isProfileComplete = false,
            isOperational = false,
            missingSteps = listOf("rubro_principal"),
        ),
    ): ResolveInitialDestinationUseCase {
        val sessionRepository = object : SessionRepository {
            override val authState: Flow<com.cocido.oficios.domain.model.AuthState> =
                flowOf(com.cocido.oficios.domain.model.AuthState.Unauthenticated)
            override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
            override suspend fun clearSession() = Unit
            override suspend fun getAccessToken() = accessToken
            override suspend fun getRefreshToken() = null
            override suspend fun updateTokens(tokens: AuthTokens) = Unit
            override suspend fun getAccessTokenExpiresAtMillis(): Long? = null
            override suspend fun getCachedUser() = null
            override suspend fun updateCachedUser(user: User) = Unit
        }
        val authRepository = object : AuthRepository {
            override suspend fun login(email: String, password: String) =
                AppResult.Error(AppError.Unknown())
            override suspend fun registerClient(request: com.cocido.oficios.domain.model.RegisterClientRequest) =
                AppResult.Error(AppError.Unknown())
            override suspend fun registerProvider(request: com.cocido.oficios.domain.model.RegisterProviderRequest) =
                AppResult.Error(AppError.Unknown())
            override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown())
            override suspend fun getCurrentUser() = userResult
            override suspend fun logout() = AppResult.Success(Unit)
            override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
            override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
            override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
                AppResult.Error(AppError.Unknown())
            override suspend fun sendVerificationEmail() = AppResult.Success("Código de verificación reenviado a tu correo")
            override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
            override suspend fun getUserRoles(): AppResult<List<com.cocido.oficios.domain.model.UserRoleDefinition>> =
                AppResult.Success(emptyList())
        }
        val onboardingRepository = object : OnboardingRepository {
            override suspend fun getClientProfileStatus() = AppResult.Success(clientStatus)
            override suspend fun getProviderProfileStatus() = AppResult.Success(providerStatus)
            override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<com.cocido.oficios.domain.model.OnboardingStep> =
                emptyList()
            override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<com.cocido.oficios.domain.model.OnboardingStep> =
                emptyList()
        }
        return ResolveInitialDestinationUseCase(sessionRepository, authRepository, onboardingRepository)
    }
}
