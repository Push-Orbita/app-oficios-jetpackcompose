package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.dto.CreateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateContactoRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateDomicilioRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateFavoriteRequestDto
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

    suspend fun createMyDomicilio(request: UpdateDomicilioRequestDto) = safeApiCall {
        clientProfileApi.createMyDomicilio(request)
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

    suspend fun updateDomicilio(id: Long, request: UpdateDomicilioRequestDto) = safeApiCall {
        clientProfileApi.updateDomicilio(id, request)
    }

    suspend fun deleteDomicilio(id: Long) = safeApiCall {
        clientProfileApi.deleteDomicilio(id)
    }

    suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean) = safeApiCall {
        clientProfileApi.setFavoriteDomicilio(id, UpdateFavoriteRequestDto(favorite))
    }

    suspend fun updateContacto(id: Long, request: UpdateContactoRequestDto) = safeApiCall {
        clientProfileApi.updateContacto(id, request)
    }

    suspend fun deleteContacto(id: Long) = safeApiCall {
        clientProfileApi.deleteContacto(id)
    }

    suspend fun setFavoriteContacto(id: Long, favorite: Boolean) = safeApiCall {
        clientProfileApi.setFavoriteContacto(id, UpdateFavoriteRequestDto(favorite))
    }
}
