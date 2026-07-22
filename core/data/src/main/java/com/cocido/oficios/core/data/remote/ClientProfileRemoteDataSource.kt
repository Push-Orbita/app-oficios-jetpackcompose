package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientProfileRemoteDataSource @Inject constructor(
    private val clientProfileApi: ClientProfileApi,
) {
    suspend fun createDomicilio(request: CreateDomicilioRequestDto) = safeApiCall {
        clientProfileApi.createDomicilio(request)
    }

    suspend fun createContacto(request: CreateContactoRequestDto) = safeApiCall {
        clientProfileApi.createContacto(request)
    }

    suspend fun sendPhoneVerificationCode(contactId: Long) = safeApiCall {
        clientProfileApi.sendPhoneVerificationCode(contactId)
    }

    suspend fun verifyPhoneCode(contactId: Long, code: String) = safeApiCall {
        clientProfileApi.verifyPhoneCode(
            contactId = contactId,
            request = com.cocido.oficios.core.network.api.profile.dto.VerifyTelefonoCodigoRequestDto(codigo = code),
        )
    }

    suspend fun getMyDomicilios() = safeApiCall {
        clientProfileApi.getMyDomicilios()
    }

    suspend fun getMyContactos() = safeApiCall {
        clientProfileApi.getMyContactos()
    }
}
