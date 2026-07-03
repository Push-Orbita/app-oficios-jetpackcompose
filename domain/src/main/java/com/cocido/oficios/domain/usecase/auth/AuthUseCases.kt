package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.SessionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import com.cocido.oficios.domain.model.AuthState

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AppResult<UserSession> =
        authRepository.login(email, password)
}

class RegisterClientUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(request: RegisterClientRequest): AppResult<UserSession> =
        authRepository.registerClient(request)
}

class RegisterProviderUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(request: RegisterProviderRequest): AppResult<UserSession> =
        authRepository.registerProvider(request)
}

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String): AppResult<Unit> =
        authRepository.requestPasswordReset(email)
}

class VerifyResetCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(code: String): AppResult<String> =
        authRepository.verifyPasswordResetCode(code)
}

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        resetToken: String,
        password: String,
        confirmPassword: String,
    ): AppResult<Unit> = authRepository.resetPassword(resetToken, password, confirmPassword)
}

class SendVerificationEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<String> = authRepository.sendVerificationEmail()
}

class VerifyEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(code: String): AppResult<Unit> {
        when (val verifyResult = authRepository.verifyEmail(code)) {
            is AppResult.Error -> return verifyResult
            is AppResult.Success -> Unit
        }
        return when (val userResult = authRepository.getCurrentUser()) {
            is AppResult.Success -> {
                sessionRepository.updateCachedUser(userResult.data)
                AppResult.Success(Unit)
            }
            is AppResult.Error -> userResult
        }
    }
}

class RefreshTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val refreshToken = sessionRepository.getRefreshToken()
            ?: return AppResult.Error(com.cocido.oficios.core.common.result.AppError.Unauthorized("Sesión expirada"))
        return when (val result = authRepository.refreshToken(refreshToken)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> result
        }
    }
}

class RefreshTokenIfNeededUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) {
    suspend operator fun invoke() {
        if (com.cocido.oficios.core.common.auth.TokenExpiry.shouldRefresh(
                sessionRepository.getAccessTokenExpiresAtMillis(),
            )
        ) {
            refreshTokenUseCase()
        }
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.getCurrentUser()
}

class ObserveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<AuthState> = sessionRepository.authState
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.logout()
}

class GetUserRolesUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.getUserRoles()
}

class ResolveInitialDestinationUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(): AppResult<InitialDestination> {
        val accessToken = sessionRepository.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            return AppResult.Success(InitialDestination.Login)
        }

        val userResult = authRepository.getCurrentUser()
        if (userResult is AppResult.Error) {
            if (userResult.error is com.cocido.oficios.core.common.result.AppError.Unauthorized) {
                sessionRepository.clearSession()
            }
            return AppResult.Success(InitialDestination.Login)
        }

        val user = (userResult as AppResult.Success).data
        return when (user.role) {
            com.cocido.oficios.domain.model.UserRole.CLIENT -> resolveClientDestination()
            com.cocido.oficios.domain.model.UserRole.PROVIDER -> resolveProviderDestination()
            else -> AppResult.Success(InitialDestination.Login)
        }
    }

    private suspend fun resolveClientDestination(): AppResult<InitialDestination> =
        when (val status = onboardingRepository.getClientProfileStatus()) {
            is AppResult.Success -> when {
                status.data.accountStatus == com.cocido.oficios.domain.model.AccountStatus.BLOCKED ->
                    AppResult.Success(InitialDestination.ClientBlocked)
                status.data.accountStatus == com.cocido.oficios.domain.model.AccountStatus.INACTIVE ->
                    AppResult.Success(InitialDestination.ClientInactive)
                !status.data.isComplete -> AppResult.Success(InitialDestination.ClientOnboarding)
                else -> AppResult.Success(InitialDestination.ClientHome)
            }
            is AppResult.Error -> AppResult.Success(InitialDestination.ClientOnboarding)
        }

    private suspend fun resolveProviderDestination(): AppResult<InitialDestination> =
        when (val status = onboardingRepository.getProviderProfileStatus()) {
            is AppResult.Success -> when (status.data.accountStatus) {
                com.cocido.oficios.domain.model.AccountStatus.REJECTED ->
                    AppResult.Success(InitialDestination.ProviderRejected)
                com.cocido.oficios.domain.model.AccountStatus.APPROVED ->
                    if (status.data.isOperational) {
                        AppResult.Success(InitialDestination.ProviderHome)
                    } else {
                        AppResult.Success(InitialDestination.ProviderOnboarding)
                    }
                com.cocido.oficios.domain.model.AccountStatus.PENDING_APPROVAL ->
                    if (!status.data.isConfigurationComplete) {
                        AppResult.Success(InitialDestination.ProviderOnboarding)
                    } else {
                        AppResult.Success(InitialDestination.ProviderPendingApproval)
                    }
                else ->
                    if (!status.data.isConfigurationComplete) {
                        AppResult.Success(InitialDestination.ProviderOnboarding)
                    } else {
                        AppResult.Success(InitialDestination.ProviderPendingApproval)
                    }
            }
            is AppResult.Error -> AppResult.Success(InitialDestination.ProviderOnboarding)
        }
}

class GetOnboardingStatusUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend fun getClientStatus() = onboardingRepository.getClientProfileStatus()
    suspend fun getProviderStatus() = onboardingRepository.getProviderProfileStatus()
    suspend fun getClientSteps(status: com.cocido.oficios.domain.model.ClientProfileStatus) =
        onboardingRepository.getClientRequiredSteps(status)
    suspend fun getProviderSteps(status: com.cocido.oficios.domain.model.ProviderProfileStatus) =
        onboardingRepository.getProviderRequiredSteps(status)
}
