package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.PhoneVerificationSendResult

interface ClientProfileRepository {
    suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit>
    suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long>
    suspend fun sendPhoneVerificationCode(contactId: Long): AppResult<PhoneVerificationSendResult>
    suspend fun verifyPhoneCode(contactId: Long, code: String): AppResult<Unit>
    suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>>
    suspend fun getMyContactos(): AppResult<List<ClientContacto>>
}
