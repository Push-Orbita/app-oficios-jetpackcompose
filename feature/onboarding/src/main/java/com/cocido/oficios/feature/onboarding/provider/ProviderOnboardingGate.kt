package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase

internal suspend fun providerIdentityGateMessage(
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
): String? = when (val status = getOnboardingStatusUseCase.getProviderStatus()) {
    is AppResult.Success -> {
        if (status.data.isIdentityVerified) {
            null
        } else {
            "Tu DNI debe estar aprobado antes de configurar rubros y servicios. " +
                "Si ya lo cargaste en el registro, quedó guardado en el dispositivo."
        }
    }
    is AppResult.Error -> null
}
