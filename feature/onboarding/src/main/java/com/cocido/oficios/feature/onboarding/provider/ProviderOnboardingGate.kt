package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase

/**
 * Ya no bloquea el alta post-identidad en la app.
 * El backend puede exigir DNI aprobado en `POST .../rubro/full` (gap documentado);
 * los ViewModels guardan draft y/o muestran el error de API con copy útil.
 */
internal suspend fun providerIdentityGateMessage(
    @Suppress("UNUSED_PARAMETER") getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
): String? = null

internal suspend fun isProviderIdentityVerified(
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
): Boolean = when (val status = getOnboardingStatusUseCase.getProviderStatus()) {
    is AppResult.Success -> status.data.isDniApproved
    is AppResult.Error -> false
}
