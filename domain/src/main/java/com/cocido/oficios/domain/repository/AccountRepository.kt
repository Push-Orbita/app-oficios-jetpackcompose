package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem

interface AccountRepository {
    suspend fun getProfile(): AppResult<AccountProfile>
    suspend fun updateProfile(request: UpdateAccountProfileRequest): AppResult<AccountProfile>
    suspend fun changePassword(email: String, password: String, confirmation: String): AppResult<Unit>

    suspend fun getClientDomicilios(): AppResult<List<ClientDomicilio>>
    suspend fun createClientDomicilio(request: ClientDomicilioRequest): AppResult<Unit>
    suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest): AppResult<Unit>
    suspend fun deleteClientDomicilio(id: Long): AppResult<Unit>
    suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean): AppResult<Unit>

    suspend fun getClientContactos(): AppResult<List<ClientContacto>>
    suspend fun createClientContacto(request: ClientContactoRequest): AppResult<Long>
    suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest): AppResult<Unit>
    suspend fun deleteClientContacto(id: Long): AppResult<Unit>
    suspend fun setFavoriteContacto(id: Long, favorite: Boolean): AppResult<Unit>

    suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit>
    suspend fun addProviderService(request: ProviderRubroFullRequest): AppResult<Unit>
    suspend fun deleteProviderService(id: Long): AppResult<Unit>

    suspend fun getCertificates(): AppResult<List<WorkCertificateItem>>
    suspend fun uploadCertificate(name: String, filePath: String): AppResult<WorkCertificateItem>
    suspend fun updateCertificate(id: Long, name: String, currentUrl: String?, filePath: String?): AppResult<Unit>
    suspend fun deleteCertificate(id: Long): AppResult<Unit>

    suspend fun getGallery(): AppResult<List<WorkGalleryItem>>
    suspend fun uploadGallery(filePaths: List<String>): AppResult<List<WorkGalleryItem>>
    suspend fun deleteGalleryItem(id: Long): AppResult<Unit>

    suspend fun getCurrentDeletionRequest(): AppResult<AccountDeletionRequest?>
    suspend fun requestAccountDeletion(params: RequestAccountDeletionParams): AppResult<AccountDeletionRequest>
    suspend fun cancelAccountDeletion(): AppResult<AccountDeletionRequest>
}
