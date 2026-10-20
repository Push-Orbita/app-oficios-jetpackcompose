package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.repository.ClientProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveClientProfileUseCasesTest {

    @Test
    fun `save domicilio propagates validation error`() = runTest {
        val useCase = SaveClientDomicilioUseCase(
            FakeClientRepository(domicilioResult = AppResult.Error(AppError.Validation("Barrio requerido"))),
        )

        val result = useCase(ClientDomicilioRequest(barrioId = 1, personaId = 2, calle = "Calle"))

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `save contacto propagates validation error`() = runTest {
        val useCase = SaveClientContactoUseCase(
            FakeClientRepository(contactoResult = AppResult.Error(AppError.Validation("Teléfono inválido"))),
        )

        val result = useCase(ClientContactoRequest(nombre = "Juan", telefonoWhatsapp = "abc"))

        assertTrue(result is AppResult.Error)
    }

    private class FakeClientRepository(
        private val domicilioResult: AppResult<Unit> = AppResult.Success(Unit),
        private val contactoResult: AppResult<Long> = AppResult.Success(1L),
    ) : ClientProfileRepository {
        override suspend fun saveDomicilio(request: ClientDomicilioRequest) = domicilioResult
        override suspend fun saveContacto(request: ClientContactoRequest) = contactoResult
        override suspend fun sendPhoneVerificationCode(contactId: Long) =
            AppResult.Success(com.cocido.oficios.domain.model.PhoneVerificationSendResult("ok", null))
        override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
        override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> = AppResult.Success(emptyList())
        override suspend fun getMyContactos(): AppResult<List<ClientContacto>> = AppResult.Success(emptyList())
    }
}
