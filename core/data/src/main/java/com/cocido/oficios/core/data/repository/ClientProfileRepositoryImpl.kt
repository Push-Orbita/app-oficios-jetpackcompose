package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toDto
import com.cocido.oficios.core.data.remote.ClientProfileRemoteDataSource
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.repository.ClientProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: ClientProfileRemoteDataSource,
) : ClientProfileRepository {

    override suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit> =
        remoteDataSource.createDomicilio(request.toDto()).map { }

    override suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long> =
        remoteDataSource.createContacto(request.toDto()).map { it.id }

    override suspend fun sendPhoneVerificationCode(contactId: Long) =
        remoteDataSource.sendPhoneVerificationCode(contactId).map { response ->
            com.cocido.oficios.domain.model.PhoneVerificationSendResult(
                message = response.message,
                resendCooldownSeconds = response.proximoReenvioEnSegundos,
            )
        }

    override suspend fun verifyPhoneCode(contactId: Long, code: String) =
        remoteDataSource.verifyPhoneCode(contactId, code).map { }

    override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> =
        remoteDataSource.getMyDomicilios().map { response ->
            response.domicilios.map { it.toDomain() }
        }

    override suspend fun getMyContactos(): AppResult<List<ClientContacto>> =
        remoteDataSource.getMyContactos().map { response ->
            response.contactos.map { it.toDomain() }
        }
}
