package com.cocido.oficios.domain.usecase.account

import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.repository.AccountRepository
import javax.inject.Inject

class AccountUseCases @Inject constructor(
    private val repository: AccountRepository,
) {
    val profile get() = repository.profile
    val providerDetails get() = repository.providerDetails
    val domicilios get() = repository.domicilios
    val contactos get() = repository.contactos

    suspend fun getProfile() = repository.getProfile()
    suspend fun updateProfile(request: UpdateAccountProfileRequest) = repository.updateProfile(request)
    suspend fun changePassword(email: String, password: String, confirmation: String) =
        repository.changePassword(email, password, confirmation)

    suspend fun getDomicilios() = repository.getClientDomicilios()
    suspend fun createDomicilio(request: ClientDomicilioRequest) = repository.createClientDomicilio(request)
    suspend fun updateDomicilio(id: Long, request: UpdateClientDomicilioRequest) =
        repository.updateClientDomicilio(id, request)
    suspend fun deleteDomicilio(id: Long) = repository.deleteClientDomicilio(id)
    suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) =
        repository.setFavoriteDomicilio(id, favorite)

    suspend fun getContactos() = repository.getClientContactos()
    suspend fun createContacto(request: ClientContactoRequest) = repository.createClientContacto(request)
    suspend fun updateContacto(id: Long, request: UpdateClientContactoRequest) =
        repository.updateClientContacto(id, request)
    suspend fun deleteContacto(id: Long) = repository.deleteClientContacto(id)
    suspend fun setFavoriteContacto(id: Long, favorite: Boolean) =
        repository.setFavoriteContacto(id, favorite)

    suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
        repository.updateProviderProfessionalInfo(request)
    suspend fun addService(request: ProviderRubroFullRequest) = repository.addProviderService(request)
    suspend fun deleteService(id: Long) = repository.deleteProviderService(id)
    suspend fun replaceService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ) = repository.replaceProviderService(existingId, isPrimary, request)

    suspend fun getCertificates() = repository.getCertificates()
    suspend fun uploadCertificate(name: String, filePath: String) =
        repository.uploadCertificate(name, filePath)
    suspend fun updateCertificate(id: Long, name: String, currentUrl: String?, filePath: String?) =
        repository.updateCertificate(id, name, currentUrl, filePath)
    suspend fun deleteCertificate(id: Long) = repository.deleteCertificate(id)

    suspend fun getGallery() = repository.getGallery()
    suspend fun uploadGallery(filePaths: List<String>) = repository.uploadGallery(filePaths)
    suspend fun deleteGalleryItem(id: Long) = repository.deleteGalleryItem(id)

    suspend fun getCurrentDeletionRequest() = repository.getCurrentDeletionRequest()
    suspend fun requestAccountDeletion(params: RequestAccountDeletionParams) =
        repository.requestAccountDeletion(params)
    suspend fun cancelAccountDeletion() = repository.cancelAccountDeletion()
}
