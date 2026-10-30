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
import com.cocido.oficios.domain.repository.WorkCertificateRepository
import javax.inject.Inject

class SubmitRegisterTradeDraftUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
    private val catalogRepository: CatalogRepository,
    private val syncRegisterTradeDraftFromProfileUseCase: SyncRegisterTradeDraftFromProfileUseCase,
) {
    suspend operator fun invoke(requireDraft: Boolean = false): AppResult<Unit> {
        var draft = registerTradeDraftRepository.get()
        if (draft == null || draft.subRubroIds.isEmpty()) {
            draft = syncRegisterTradeDraftFromProfileUseCase()
        }
        if (draft == null) {
            return if (requireDraft) {
                AppResult.Error(
                    ClientValidationException(
                        "No se encontraron los datos del rubro. Vuelva al paso anterior y complete los de nuevo.",
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

class SyncRegisterTradeDraftFromProfileUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
    private val registerTradeDraftRepository: RegisterTradeDraftRepository,
) {
    suspend operator fun invoke(): RegisterTradeDraft? {
        val local = registerTradeDraftRepository.get()
        val remote = when (val result = providerProfileRepository.loadProviderProfileForSync()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }
        val merged = mergeDrafts(local, remote?.tradeDraft, remote?.paymentMethods.orEmpty())
        merged?.let { registerTradeDraftRepository.save(it) }
        return merged
    }

    private fun mergeDrafts(
        local: RegisterTradeDraft?,
        remote: RegisterTradeDraft?,
        remotePaymentMethods: List<String>,
    ): RegisterTradeDraft? {
        val remoteHasTrade = remote != null && remote.rubroId > 0 && remote.subRubroIds.isNotEmpty()
        val base = when {
            remoteHasTrade -> remote
            remote == null -> local
            else -> local
        }
        if (base == null) return null

        val paymentMethods = when {
            remotePaymentMethods.isNotEmpty() -> remotePaymentMethods
            remote?.paymentMethods?.isNotEmpty() == true -> remote.paymentMethods
            else -> local?.paymentMethods.orEmpty()
        }

        return base.copy(
            paymentMethods = paymentMethods,
            otherSpecialization = base.otherSpecialization ?: local?.otherSpecialization,
            experienceRange = base.experienceRange.ifBlank { local?.experienceRange.orEmpty() },
            neighborhoodQuery = base.neighborhoodQuery.ifBlank { local?.neighborhoodQuery.orEmpty() },
            barrioId = base.barrioId ?: local?.barrioId,
        )
    }
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
    suspend operator fun invoke(request: ClientContactoRequest): AppResult<Long> =
        clientProfileRepository.saveContacto(request)
}

class GetClientDomiciliosUseCase @Inject constructor(
    private val clientProfileRepository: ClientProfileRepository,
) {
    suspend operator fun invoke() = clientProfileRepository.getMyDomicilios()
}

class GetClientContactosUseCase @Inject constructor(
    private val clientProfileRepository: ClientProfileRepository,
) {
    suspend operator fun invoke() = clientProfileRepository.getMyContactos()
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

class GetProviderDniDocumentsUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke() = identityDocumentRepository.getMyDniDocuments()
}

class UploadProviderSelfieUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke(imagePath: String): AppResult<Unit> =
        identityDocumentRepository.uploadSelfie(imagePath)
}

class ReplaceProviderSelfieUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke(documentId: Long, imagePath: String): AppResult<Unit> =
        identityDocumentRepository.replaceSelfie(documentId, imagePath)
}

class ReplaceProviderDniImagesUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke(
        documentId: Long,
        frontImagePath: String?,
        backImagePath: String?,
    ): AppResult<Unit> = identityDocumentRepository.replaceDni(documentId, frontImagePath, backImagePath)
}

class DeleteProviderIdentityDocumentUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke(documentId: Long): AppResult<Unit> =
        identityDocumentRepository.deleteDocument(documentId)
}

class GetProviderSelfieUseCase @Inject constructor(
    private val identityDocumentRepository: IdentityDocumentRepository,
) {
    suspend operator fun invoke() = identityDocumentRepository.getMySelfieDocument()
}

class GetProviderDocumentoUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(): String? = when (
        val result = providerProfileRepository.loadProviderProfileForSync()
    ) {
        is AppResult.Success -> result.data.documento
        is AppResult.Error -> null
    }
}

class SendContactPhoneVerificationCodeUseCase @Inject constructor(
    private val clientProfileRepository: ClientProfileRepository,
) {
    suspend operator fun invoke(contactId: Long) =
        clientProfileRepository.sendPhoneVerificationCode(contactId)
}

class VerifyContactPhoneCodeUseCase @Inject constructor(
    private val clientProfileRepository: ClientProfileRepository,
) {
    suspend operator fun invoke(contactId: Long, code: String): AppResult<Unit> =
        clientProfileRepository.verifyPhoneCode(contactId, code)
}

class UploadWorkCertificateUseCase @Inject constructor(
    private val workCertificateRepository: WorkCertificateRepository,
) {
    suspend operator fun invoke(name: String, filePath: String) =
        workCertificateRepository.uploadCertificate(name, filePath)
}

class GetWorkCertificatesUseCase @Inject constructor(
    private val workCertificateRepository: WorkCertificateRepository,
) {
    suspend operator fun invoke() = workCertificateRepository.getMyCertificates()
}

class SubmitProviderRubroFullUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(request: ProviderRubroFullRequest): AppResult<Unit> =
        providerProfileRepository.submitRubroFull(request)
}

class SubmitSecondaryProviderRubroUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(request: ProviderRubroFullRequest): AppResult<Unit> =
        providerProfileRepository.submitSecondaryRubroFull(request)
}

class FinalizeProviderProfileUseCase @Inject constructor(
    private val providerProfileRepository: ProviderProfileRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = providerProfileRepository.finalizeProfile()
}
