package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilioRequest

interface ClientProfileRepository {
    suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit>
    suspend fun saveContacto(request: ClientContactoRequest): AppResult<Unit>
}
