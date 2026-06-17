package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.ClientValidationException
import com.cocido.oficios.core.common.result.toAppError
import com.cocido.oficios.domain.mapper.toProfessionalInfoRequest
import com.cocido.oficios.domain.mapper.toRubroFullRequest
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.repository.IdentityDocumentRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import javax.inject.Inject

class SubmitRegisterTradeDraftUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(requireDraft: Boolean = false): AppResult<Unit> {
        var draft = registerTradeDraftRepository.get()
        if (draft == null) {
            return if (requireDraft) {
                AppResult.Error(
                    ClientValidationException(
                        "No se encontraron los datos del rubro. Volvé al paso anterior y completalos de nuevo.",
                    ).toAppError(),
                )
            } else {
                AppResult.Success(Unit)
            }
        }
        draft = resolveBarrioId(draft)

        val infoResult = providerProfileRepository.updateProfessionalInfo(draft.toProfessionalInfoRequest())
        if (infoResult is AppResult.Error) return infoResult

        return when (val rubroResult = providerProfileRepository.submitRubroFull(draft.toRubroFullRequest())) {
            is AppResult.Success -> {
                AppResult.Success(Unit)
            }
            is AppResult.Error -> rubroResult
        }
    }

    private suspend fun resolveBarrioId(draft: RegisterTradeDraft): RegisterTradeDraft {
        if (draft.barrioId != null || draft.neighborhoodQuery.isBlank()) return draft
        return when (val result = catalogRepository.searchBarrios(draft.neighborhoodQuery)) {
            is AppResult.Success -> {
                val barrioId = result.data.firstOrNull()?.id
                if (barrioId != null) {
                    val updated = draft.copy(barrioId = barrioId)
                    registerTradeDraftRepository.save(updated)
                    updated
                } else {
                    draft
                }
            }
            is AppResult.Error -> draft
        }
    }
}

class RetryPendingTradeDraftUseCase @Inject constructor(
    private val submitRegisterTradeDraftUseCase: SubmitRegisterTradeDraftUseCase,
) {
    suspend operator fun invoke(): AppResult<Unit> = submitRegisterTradeDraftUseCase()
}

class SaveClientDomicilioUseCase @Inject constructor(
    private val clientProfileRepository: ClientProfileRepository,
) {
    suspend operator fun invoke(request: ClientDomicilioRequest): AppResult<Unit> =
        clientProfileRepository.saveDomicilio(request)
}

class SaveClientContactoUseCase @Inject constructor(
    private val clientProfileRepository: ClientProfileRepository,
) {
    suspend operator fun invoke(request: ClientContactoRequest): AppResult<Unit> =
        clientProfileRepository.saveContacto(request)
}

class UpdateProviderProfessionalInfoUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(request: ProviderProfessionalInfoRequest): AppResult<Unit> =
        providerProfileRepository.updateProfessionalInfo(request)
}

class UpdateProviderDniNumberUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(documento: String): AppResult<Unit> =
        providerProfileRepository.updateProfessionalInfo(
            ProviderProfessionalInfoRequest(documento = documento),
        )
}

class UploadProviderDniImagesUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke(frontImagePath: String, backImagePath: String): AppResult<Unit> =
        identityDocumentRepository.uploadDniImages(frontImagePath, backImagePath)
}

class SubmitProviderRubroFullUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(request: ProviderRubroFullRequest): AppResult<Unit> =
        providerProfileRepository.submitRubroFull(request)
}

class FinalizeProviderProfileUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = providerProfileRepository.finalizeProfile()
}
