package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.cache.CachedValue
import com.cocido.oficios.core.data.cache.ClientProfileStore
import com.cocido.oficios.core.data.cache.SessionCacheCoordinator
import com.cocido.oficios.core.data.mapper.toAccountDetails
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toDomainItem
import com.cocido.oficios.core.data.mapper.toDomainUser
import com.cocido.oficios.core.data.mapper.toDto
import com.cocido.oficios.core.data.remote.AccountRemoteDataSource
import com.cocido.oficios.core.data.remote.AgendaRemoteDataSource
import com.cocido.oficios.core.data.remote.ClientProfileRemoteDataSource
import com.cocido.oficios.core.data.remote.ProviderProfileRemoteDataSource
import com.cocido.oficios.core.data.remote.WorkCertificateRemoteDataSource
import com.cocido.oficios.core.data.remote.WorkGalleryRemoteDataSource
import com.cocido.oficios.core.data.session.SessionManager
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ManagedProviderSubService
import com.cocido.oficios.domain.model.ProviderAccountDetails
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountRemote: AccountRemoteDataSource,
    private val clientRemote: ClientProfileRemoteDataSource,
    private val providerRemote: ProviderProfileRemoteDataSource,
    private val agendaRemote: AgendaRemoteDataSource,
    private val certificateRemote: WorkCertificateRemoteDataSource,
    private val galleryRemote: WorkGalleryRemoteDataSource,
    private val sessionManager: SessionManager,
    coordinator: SessionCacheCoordinator,
    private val clientProfileStore: ClientProfileStore,
) : AccountRepository {

    private val profileCache = CachedValue<AccountProfile>(coordinator)
    private val providerDetailsCache = CachedValue<ProviderAccountDetails>(coordinator)

    override val profile: StateFlow<AccountProfile?> = profileCache.state
    override val providerDetails: StateFlow<ProviderAccountDetails?> = providerDetailsCache.state
    override val domicilios: StateFlow<List<com.cocido.oficios.domain.model.ClientDomicilio>?> =
        clientProfileStore.domicilios.state
    override val contactos: StateFlow<List<com.cocido.oficios.domain.model.ClientContacto>?> =
        clientProfileStore.contactos.state

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
        val profile = AccountProfile(user = user, providerDetails = providerDetails)
        cacheProfile(profile)
        return AppResult.Success(profile)
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
                        is AppResult.Error -> providerDetailsCache.get()
                    }
                } else {
                    null
                }
                val profile = AccountProfile(user, providerDetails)
                cacheProfile(profile)
                AppResult.Success(profile)
            }
            is AppResult.Error -> result
        }

    override suspend fun changePassword(email: String, password: String, confirmation: String) =
        accountRemote.changePassword(email, password, confirmation).map { Unit }

    override suspend fun getClientDomicilios() =
        clientRemote.getMyDomicilios().map { response -> response.domicilios.map { it.toDomain() } }
            .also { if (it is AppResult.Success) clientProfileStore.domicilios.set(it.data) }

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
        ).map { Unit }.also { if (it is AppResult.Success) refreshDomicilios() }

    override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest) =
        clientRemote.updateDomicilio(id, request.toDto()).map { Unit }
            .also { if (it is AppResult.Success) refreshDomicilios() }

    override suspend fun deleteClientDomicilio(id: Long): AppResult<Unit> {
        val previous = clientProfileStore.domicilios.get()
        clientProfileStore.domicilios.update { list -> list.filterNot { it.id == id } }
        return when (val result = clientRemote.deleteDomicilio(id).map { Unit }) {
            is AppResult.Success -> {
                refreshDomicilios()
                result
            }
            is AppResult.Error -> {
                clientProfileStore.domicilios.set(previous)
                result
            }
        }
    }

    override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) =
        clientRemote.setFavoriteDomicilio(id, favorite).map { Unit }
            .also { if (it is AppResult.Success) refreshDomicilios() }

    override suspend fun getClientContactos() =
        clientRemote.getMyContactos().map { response -> response.contactos.map { it.toDomain() } }
            .also { if (it is AppResult.Success) clientProfileStore.contactos.set(it.data) }

    override suspend fun createClientContacto(request: ClientContactoRequest) =
        clientRemote.createContacto(request.toDto()).map { it.id }
            .also { if (it is AppResult.Success) refreshContactos() }

    override suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest) =
        clientRemote.updateContacto(id, request.toDto()).map { Unit }
            .also { if (it is AppResult.Success) refreshContactos() }

    override suspend fun deleteClientContacto(id: Long): AppResult<Unit> {
        val previous = clientProfileStore.contactos.get()
        clientProfileStore.contactos.update { list -> list.filterNot { it.id == id } }
        return when (val result = clientRemote.deleteContacto(id).map { Unit }) {
            is AppResult.Success -> {
                refreshContactos()
                result
            }
            is AppResult.Error -> {
                clientProfileStore.contactos.set(previous)
                result
            }
        }
    }

    override suspend fun setFavoriteContacto(id: Long, favorite: Boolean) =
        clientRemote.setFavoriteContacto(id, favorite).map { Unit }
            .also { if (it is AppResult.Success) refreshContactos() }

    override suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest) =
        providerRemote.updateProfessionalInfo(request.toDto()).map { Unit }
            .also { if (it is AppResult.Success) refreshProviderDetails() }

    override suspend fun addProviderService(request: ProviderRubroFullRequest): AppResult<Unit> {
        val details = providerDetailsCache.get() ?: when (val refreshed = refreshProviderDetails()) {
            is AppResult.Success -> refreshed.data
            is AppResult.Error -> null
        }
        val created = if (details?.services?.any { it.isPrimary } == true) {
            providerRemote.createSecondaryRubroFull(request.toDto())
        } else {
            providerRemote.createRubroFull(request.toDto())
        }
        return created.map { Unit }.also { if (it is AppResult.Success) refreshProviderDetails() }
    }

    override suspend fun deleteProviderService(id: Long): AppResult<Unit> {
        val previous = providerDetailsCache.get()
        previous?.let { details ->
            cacheProviderDetails(details.copy(services = details.services.filterNot { it.id == id }))
        }
        return when (val result = providerRemote.deleteRubro(id).map { Unit }) {
            is AppResult.Success -> {
                refreshProviderDetails()
                result
            }
            is AppResult.Error -> {
                previous?.let { cacheProviderDetails(it) }
                result
            }
        }
    }

    override suspend fun replaceProviderService(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ): AppResult<Unit> {
        val existing = providerDetailsCache.get()?.services?.firstOrNull { it.id == existingId }
        if (existing != null) {
            return updateExistingProviderService(existing, request)
        }
        return createThenDeleteOld(existingId, isPrimary, request)
    }

    private suspend fun updateExistingProviderService(
        existing: ManagedProviderService,
        request: ProviderRubroFullRequest,
    ): AppResult<Unit> {
        if (request.paymentMethods.isNotEmpty()) {
            when (
                val payments = providerRemote.updateProfessionalInfo(
                    UpdateTrabajadorPerfilProfesionalRequestDto(metodosPago = request.paymentMethods),
                )
            ) {
                is AppResult.Error -> return payments.map { }
                is AppResult.Success -> Unit
            }
        }
        if (request.requiresTurns) {
            when (val sessions = ensureSesiones(existing.subServices)) {
                is AppResult.Error -> return sessions
                is AppResult.Success -> Unit
            }
        }
        return refreshProviderDetails().map { }
    }

    private suspend fun ensureSesiones(
        subs: List<ManagedProviderSubService>,
    ): AppResult<Unit> {
        for (sub in subs.filter { it.id > 0L }) {
            when (val sessions = agendaRemote.getSesionesPorSubRubro(sub.id)) {
                is AppResult.Error -> return sessions.map { }
                is AppResult.Success -> if (sessions.data.isEmpty()) {
                    when (
                        val created = agendaRemote.createSesion(
                            subRubroId = sub.id,
                            duracion = DEFAULT_TURN_DURATION_MINUTES,
                            medidaDuracion = DEFAULT_TURN_DURATION_UNIT,
                            intervalo = DEFAULT_TURN_INTERVAL,
                            medidaIntervalo = DEFAULT_TURN_DURATION_UNIT,
                        )
                    ) {
                        is AppResult.Error -> return created.map { }
                        is AppResult.Success -> Unit
                    }
                }
            }
        }
        return AppResult.Success(Unit)
    }

    private suspend fun createThenDeleteOld(
        existingId: Long,
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ): AppResult<Unit> {
        val created = createRubro(isPrimary, request)
        return deleteOnlyAfterCreateSucceeds(created) {
            providerRemote.deleteRubro(existingId).also { deleted ->
                if (deleted is AppResult.Success) refreshProviderDetails()
            }
        }
    }

    private suspend fun createRubro(
        isPrimary: Boolean,
        request: ProviderRubroFullRequest,
    ) = if (isPrimary) {
        providerRemote.createRubroFull(request.toDto())
    } else {
        providerRemote.createSecondaryRubroFull(request.toDto())
    }

    override suspend fun getCertificates() = refreshProviderDetails().map { it.certificates }

    override suspend fun uploadCertificate(name: String, filePath: String): AppResult<WorkCertificateItem> =
        when (val result = certificateRemote.uploadCertificate(name, filePath).map { it.toDomainItem() }) {
            is AppResult.Success -> {
                providerDetailsCache.update { details ->
                    details.copy(certificates = details.certificates + result.data)
                }
                patchProfileDetails()
                refreshProviderDetails()
                result
            }
            is AppResult.Error -> result
        }

    override suspend fun updateCertificate(
        id: Long,
        name: String,
        currentUrl: String?,
        filePath: String?,
    ) = certificateRemote.updateCertificate(id, name, currentUrl, filePath).map { Unit }
        .also { if (it is AppResult.Success) refreshProviderDetails() }

    override suspend fun deleteCertificate(id: Long): AppResult<Unit> {
        val previous = providerDetailsCache.get()
        previous?.let { details ->
            cacheProviderDetails(details.copy(certificates = details.certificates.filterNot { it.id == id }))
        }
        return when (val result = certificateRemote.deleteCertificate(id).map { Unit }) {
            is AppResult.Success -> {
                refreshProviderDetails()
                result
            }
            is AppResult.Error -> {
                previous?.let { cacheProviderDetails(it) }
                result
            }
        }
    }

    override suspend fun getGallery() = refreshProviderDetails().map { it.gallery }

    override suspend fun uploadGallery(filePaths: List<String>): AppResult<List<WorkGalleryItem>> =
        when (val result = galleryRemote.upload(filePaths).map { items -> items.map { it.toDomainItem() } }) {
            is AppResult.Success -> {
                providerDetailsCache.update { details ->
                    details.copy(gallery = details.gallery + result.data)
                }
                patchProfileDetails()
                refreshProviderDetails()
                result
            }
            is AppResult.Error -> result
        }

    override suspend fun deleteGalleryItem(id: Long): AppResult<Unit> {
        val previous = providerDetailsCache.get()
        previous?.let { details ->
            cacheProviderDetails(details.copy(gallery = details.gallery.filterNot { it.id == id }))
        }
        return when (val deleted = galleryRemote.delete(id)) {
            is AppResult.Error -> {
                previous?.let { cacheProviderDetails(it) }
                deleted
            }
            is AppResult.Success -> {
                refreshProviderDetails()
                AppResult.Success(Unit)
            }
        }
    }

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

    private fun cacheProfile(profile: AccountProfile) {
        profileCache.set(profile)
        profile.providerDetails?.let { providerDetailsCache.set(it) }
    }

    private fun cacheProviderDetails(details: ProviderAccountDetails) {
        providerDetailsCache.set(details)
        patchProfileDetails()
    }

    private fun patchProfileDetails() {
        val details = providerDetailsCache.get() ?: return
        profileCache.update { it.copy(providerDetails = details) }
    }

    private suspend fun refreshProviderDetails(): AppResult<ProviderAccountDetails> =
        when (val result = providerRemote.getProviderProfile()) {
            is AppResult.Success -> {
                val details = result.data.toAccountDetails()
                cacheProviderDetails(details)
                AppResult.Success(details)
            }
            is AppResult.Error -> result
        }

    private suspend fun refreshDomicilios() {
        getClientDomicilios()
    }

    private suspend fun refreshContactos() {
        getClientContactos()
    }
}

internal suspend fun deleteOnlyAfterCreateSucceeds(
    created: AppResult<*>,
    deleteOld: suspend () -> AppResult<*>,
): AppResult<Unit> = when (created) {
    is AppResult.Error -> created.map { }
    is AppResult.Success -> deleteOld().map { }
}

private const val DEFAULT_TURN_DURATION_MINUTES = 60
private const val DEFAULT_TURN_DURATION_UNIT = "minutos"
private const val DEFAULT_TURN_INTERVAL = "15"
