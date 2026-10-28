package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.PhoneVerificationSendResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val EmptyClientDomicilios: StateFlow<List<ClientDomicilio>?> =
    MutableStateFlow<List<ClientDomicilio>?>(null).asStateFlow()
private val EmptyClientContactos: StateFlow<List<ClientContacto>?> =
    MutableStateFlow<List<ClientContacto>?>(null).asStateFlow()

interface ClientProfileRepository {
    val domicilios: StateFlow<List<ClientDomicilio>?> get() = EmptyClientDomicilios
    val contactos: StateFlow<List<ClientContacto>?> get() = EmptyClientContactos

    suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit>
    suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long>
    suspend fun sendPhoneVerificationCode(contactId: Long): AppResult<PhoneVerificationSendResult>
    suspend fun verifyPhoneCode(contactId: Long, code: String): AppResult<Unit>
    suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>>
    suspend fun getMyContactos(): AppResult<List<ClientContacto>>
}
