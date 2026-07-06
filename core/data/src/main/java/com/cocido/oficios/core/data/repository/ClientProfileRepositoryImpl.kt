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

    override suspend fun saveContacto(request: ClientContactoRequest): AppResult<Unit> =
        remoteDataSource.createContacto(request.toDto()).map { }

    override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> =
        remoteDataSource.getMyDomicilios().map { response ->
            response.domicilios.map { it.toDomain() }
        }

    override suspend fun getMyContactos(): AppResult<List<ClientContacto>> =
        remoteDataSource.getMyContactos().map { response ->
            response.contactos.map { it.toDomain() }
        }
}
