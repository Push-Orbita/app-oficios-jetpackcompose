package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toAccountDetails
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toDomainItem
import com.cocido.oficios.core.data.mapper.toDomainUser
import com.cocido.oficios.core.data.mapper.toDto
import com.cocido.oficios.core.data.remote.AccountRemoteDataSource
import com.cocido.oficios.core.data.remote.ClientProfileRemoteDataSource
import com.cocido.oficios.core.data.remote.ProviderProfileRemoteDataSource
import com.cocido.oficios.core.data.remote.WorkCertificateRemoteDataSource
import com.cocido.oficios.core.data.remote.WorkGalleryRemoteDataSource
import com.cocido.oficios.core.data.session.SessionManager
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountRemote: AccountRemoteDataSource,
    private val clientRemote: ClientProfileRemoteDataSource,
    private val providerRemote: ProviderProfileRemoteDataSource,
    private val certificateRemote: WorkCertificateRemoteDataSource,
    private val galleryRemote: WorkGalleryRemoteDataSource,
    private val sessionManager: SessionManager,
) : AccountRepository {

    override suspend fun getProfile(): AppResult<AccountProfile> {
        val user = when (val result = accountRemote.getCurrentUser()) {
            is AppResult.Success -> result.data.toDomainUser()
            is AppResult.Error -> return result
        }
        val providerDetails = if (user.role == UserRole.PROVIDER) {
            when (val result = providerRemote.getProviderProfile()) {
                is AppResult.Success -> result.data.toAccountDetails()
                is AppResult.Error -> return result
            }
        } else {
            null
        }
        return AppResult.Success(AccountProfile(user = user, providerDetails = providerDetails))
    }

    override suspend fun updateProfile(request: UpdateAccountProfileRequest): AppResult<AccountProfile> =
        when (val result = accountRemote.updateProfile(request)) {
            is AppResult.Success -> {
                val responseUser = result.data.toDomainUser()
                val cachedUser = sessionManager.getCachedUser()
                // Algunos despliegues no incluyen rol/persona en PATCH /usuario/{id}.
                // Conservamos esos datos de sesión para no degradar la navegación tras editar el perfil.
                val user = responseUser.copy(
                    role = responseUser.role.takeUnless { it == UserRole.UNKNOWN } ?: cachedUser?.role ?: UserRole.UNKNOWN,
                    personaId = responseUser.personaId ?: cachedUser?.personaId,
                    profilePhotoUrl = responseUser.profilePhotoUrl
                        ?: request.currentPhotoUrl?.takeUnless { request.removePhoto }
                        ?: cachedUser?.profilePhotoUrl?.takeUnless { request.removePhoto },
                )
                sessionManager.updateCachedUser(user)
                val providerDetails = if (user.role == UserRole.PROVIDER) {
                    when (val providerResult = providerRemote.getProviderProfile()) {
                        is AppResult.Success -> providerResult.data.toAccountDetails()
                        is AppResult.Error -> null
                    }
                } else {
                    null
                }
                AppResult.Success(AccountProfile(user, providerDetails))
            }
            is AppResult.Error -> result
        }

    override suspend fun changePassword(email: String, password: String, confirmation: String) =
        accountRemote.changePassword(email, password, confirmation).map { Unit }

    override suspend fun getClientDomicilios() =
        clientRemote.getMyDomicilios().map { response -> response.domicilios.map { it.toDomain() } }

    override suspend fun createClientDomicilio(request: ClientDomicilioRequest) =
        clientRemote.createMyDomicilio(
            UpdateClientDomicilioRequest(
                barrioId = request.barrioId,
                nombre = request.nombre,
                calle = request.calle,
                numero = request.numero,
                casaTorre = request.casaTorre,
                departamento = request.departamento,
                piso = request.piso,
                latitud = request.latitud,
                longitud = request.longitud,
                favorito = request.favorito,
            ).toDto(),
        ).map { Unit }

    override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest) =
        clientRemote.updateDomicilio(id, request.toDto()).map { Unit }

    override suspend fun deleteClientDomicilio(id: Long) = clientRemote.deleteDomicilio(id).map { Unit }

    override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) =
        clientRemote.setFavoriteDomicilio(id, favorite).map { Unit }

    override suspend fun getClientContactos() =
        clientRemote.getMyContactos().map { response -> response.contactos.map { it.toDomain() } }

    override suspend fun createClientContacto(request: ClientContactoRequest) =
        clientRemote.createContacto(request.toDto()).map { it.id }

    override suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest) =
        clientRemote.updateContacto(id, request.toDto()).map { Unit }

    override suspend fun deleteClientContacto(id: Long) = clientRemote.deleteContacto(id).map { Unit }

    override suspend fun setFavoriteContacto(id: Long, favorite: Boolean) =
        clientRemote.setFavoriteContacto(id, favorite).map { Unit }

    override suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest) =
        providerRemote.updateProfessionalInfo(request.toDto()).map { Unit }

    override suspend fun addProviderService(request: ProviderRubroFullRequest) =
        providerRemote.createSecondaryRubroFull(request.toDto()).map { Unit }

    override suspend fun deleteProviderService(id: Long) = providerRemote.deleteRubro(id).map { Unit }

    override suspend fun replaceProviderService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ): AppResult<Unit> {
        when (val deleted = providerRemote.deleteRubro(existingId)) {
            is AppResult.Error -> return deleted
            is AppResult.Success -> Unit
        }
        val created = if (isPrimary) {
            providerRemote.createRubroFull(request.toDto())
        } else {
            providerRemote.createSecondaryRubroFull(request.toDto())
        }
        return created.map { Unit }
    }

    override suspend fun getCertificates() = providerRemote.getProviderProfile().map { profile ->
        profile.certificados.map { it.toDomainItem() }
    }

    override suspend fun uploadCertificate(name: String, filePath: String) =
        certificateRemote.uploadCertificate(name, filePath).map { it.toDomainItem() }

    override suspend fun updateCertificate(
        id: Long,
        name: String,
        currentUrl: String?,
        filePath: String?,
    ) = certificateRemote.updateCertificate(id, name, currentUrl, filePath).map { Unit }

    override suspend fun deleteCertificate(id: Long) = certificateRemote.deleteCertificate(id).map { Unit }

    override suspend fun getGallery() = providerRemote.getProviderProfile().map { profile ->
        profile.galeriaTrabajos.map { it.toDomainItem() }
    }

    override suspend fun uploadGallery(filePaths: List<String>) =
        galleryRemote.upload(filePaths).map { items -> items.map { it.toDomainItem() } }

    override suspend fun deleteGalleryItem(id: Long) = galleryRemote.delete(id).map { Unit }

    override suspend fun getCurrentDeletionRequest(): AppResult<AccountDeletionRequest?> =
        when (val result = accountRemote.getCurrentDeletionRequest()) {
            is AppResult.Success -> AppResult.Success(result.data.toDomain())
            is AppResult.Error -> if (result.error is AppError.NotFound) {
                AppResult.Success(null)
            } else {
                result
            }
        }

    override suspend fun requestAccountDeletion(params: RequestAccountDeletionParams) =
        accountRemote.createDeletionRequest(
            currentPassword = params.currentPassword,
            confirmation = params.confirmation,
            reasonCode = params.reasonCode,
            idempotencyKey = params.idempotencyKey,
        ).map { it.toDomain() }

    override suspend fun cancelAccountDeletion() =
        accountRemote.cancelCurrentDeletionRequest().map { it.toDomain() }
}
