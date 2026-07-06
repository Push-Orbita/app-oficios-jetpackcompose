package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.userMessage
import com.cocido.oficios.domain.model.ProviderRegistrationOutcome
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase
import javax.inject.Inject

/**
 * Registro de proveedor: crea la cuenta y persiste rubro/especialidades en el backend.
 * Si el alta de cuenta sale bien pero falla el sync del perfil, la cuenta queda creada
 * y el borrador local se conserva para completar desde el onboarding.
 */
class CompleteProviderRegistrationUseCase @Inject constructor(
    private val registerProviderUseCase: RegisterProviderUseCase,
    private val submitRegisterTradeDraftUseCase: SubmitRegisterTradeDraftUseCase,
) {
    suspend operator fun invoke(request: RegisterProviderRequest): AppResult<ProviderRegistrationOutcome> {
        when (val registration = registerProviderUseCase(request)) {
            is AppResult.Error -> return registration
            is AppResult.Success -> {
                when (val profileSync = submitRegisterTradeDraftUseCase(requireDraft = true)) {
                    is AppResult.Error -> {
                        return AppResult.Success(
                            ProviderRegistrationOutcome(
                                session = registration.data,
                                profileSynced = false,
                                profileSyncMessage = profileSync.error.toProviderRegistrationSyncMessage(),
                            ),
                        )
                    }
                    is AppResult.Success -> {
                        return AppResult.Success(
                            ProviderRegistrationOutcome(
                                session = registration.data,
                                profileSynced = true,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun com.cocido.oficios.core.common.result.AppError.toProviderRegistrationSyncMessage(): String {
    val message = userMessage()
    return if (
        message.contains("identidad", ignoreCase = true) ||
        message.contains("documentación", ignoreCase = true) ||
        message.contains("documentacion", ignoreCase = true)
    ) {
        "Cuenta creada. Tu rubro quedó guardado y se enviará al servidor cuando aprueben tu DNI."
    } else {
        message
    }
}
