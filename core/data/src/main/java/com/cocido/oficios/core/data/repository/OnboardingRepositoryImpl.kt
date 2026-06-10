package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.mapper.toClientAccountStatus
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toOnboardingSteps
import com.cocido.oficios.core.data.mapper.toRejectionReason
import com.cocido.oficios.core.data.remote.AuthRemoteDataSource
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.repository.OnboardingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
) : OnboardingRepository {

    override suspend fun getClientProfileStatus(): AppResult<ClientProfileStatus> {
        val profileResult = remoteDataSource.getClientProfile()
        val statusResult = remoteDataSource.getClientProfileStatus()

        return when {
            statusResult is AppResult.Success && profileResult is AppResult.Success -> {
                AppResult.Success(
                    statusResult.data.toDomain(profileResult.data.estado.toClientAccountStatus()),
                )
            }
            statusResult is AppResult.Success -> {
                AppResult.Success(statusResult.data.toDomain(com.cocido.oficios.domain.model.AccountStatus.UNKNOWN))
            }
            statusResult is AppResult.Error -> statusResult
            profileResult is AppResult.Error -> profileResult
            else -> AppResult.Error(
                com.cocido.oficios.core.common.result.AppError.Unknown(),
            )
        }
    }

    override suspend fun getProviderProfileStatus(): AppResult<ProviderProfileStatus> {
        val profileResult = remoteDataSource.getProviderProfile()
        val statusResult = remoteDataSource.getProviderProfileStatus()

        val rejectionReason = (profileResult as? AppResult.Success)?.data?.toRejectionReason()

        return when (statusResult) {
            is AppResult.Success -> AppResult.Success(statusResult.data.toDomain(rejectionReason))
            is AppResult.Error -> statusResult
        }
    }

    override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> =
        status.toOnboardingSteps()

    override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> =
        status.toOnboardingSteps()
}
