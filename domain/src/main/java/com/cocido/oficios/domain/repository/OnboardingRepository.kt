package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.ProviderProfileStatus

interface OnboardingRepository {
    suspend fun getClientProfileStatus(): AppResult<ClientProfileStatus>
    suspend fun getProviderProfileStatus(): AppResult<ProviderProfileStatus>
    suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep>
    suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep>
}
