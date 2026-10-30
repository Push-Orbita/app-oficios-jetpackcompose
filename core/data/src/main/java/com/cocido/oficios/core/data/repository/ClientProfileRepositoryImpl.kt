package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.cache.ClientProfileStore
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toDto
import com.cocido.oficios.core.data.mapper.toUpdateDto
import com.cocido.oficios.core.data.remote.ClientProfileRemoteDataSource
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.repository.ClientProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ClientProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: ClientProfileRemoteDataSource,
    private val store: ClientProfileStore,
) : ClientProfileRepository {

    override val domicilios: StateFlow<List<ClientDomicilio>?> = store.domicilios.state
    override val contactos: StateFlow<List<ClientContacto>?> = store.contactos.state

    override suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit> =
        remoteDataSource.createDomicilio(request.toDto()).map { }
            .also { if (it is AppResult.Success) getMyDomicilios() }

    override suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long> =
        remoteDataSource.createContacto(request.toDto()).map { it.id }
            .also { if (it is AppResult.Success) getMyContactos() }

    override suspend fun updateDomicilio(id: Long, request: ClientDomicilioRequest): AppResult<Unit> =
        remoteDataSource.updateDomicilio(id, request.toUpdateDto())
            .also { if (it is AppResult.Success) getMyDomicilios() }

    override suspend fun updateContacto(id: Long, request: ClientContactoRequest): AppResult<Unit> =
        remoteDataSource.updateContacto(id, request.toUpdateDto())
            .also { if (it is AppResult.Success) getMyContactos() }

    override suspend fun sendPhoneVerificationCode(contactId: Long) =
        remoteDataSource.sendPhoneVerificationCode(contactId).map { response ->
            com.cocido.oficios.domain.model.PhoneVerificationSendResult(
                message = response.message,
                resendCooldownSeconds = response.proximoReenvioEnSegundos,
            )
        }

    override suspend fun verifyPhoneCode(contactId: Long, code: String) =
        remoteDataSource.verifyPhoneCode(contactId, code).map { }
            .also { if (it is AppResult.Success) getMyContactos() }

    override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> =
        remoteDataSource.getMyDomicilios().map { response ->
            response.domicilios.map { it.toDomain() }
        }.also { if (it is AppResult.Success) store.domicilios.set(it.data) }

    override suspend fun getMyContactos(): AppResult<List<ClientContacto>> =
        remoteDataSource.getMyContactos().map { response ->
            response.contactos.map { it.toDomain() }
        }.also { if (it is AppResult.Success) store.contactos.set(it.data) }
}
