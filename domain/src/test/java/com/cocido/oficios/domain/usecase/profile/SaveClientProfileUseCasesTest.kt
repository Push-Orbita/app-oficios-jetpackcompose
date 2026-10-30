package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.repository.ClientProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    @Test
    fun `save contacto creates when there is no contacto`() = runTest {
        val repo = FakeClientRepository(contactoResult = AppResult.Success(15L))

        val result = SaveClientContactoUseCase(repo)(request(phone = "93704123456"))

        assertEquals(AppResult.Success(15L), result)
        assertEquals(1, repo.createdContactos.size)
        assertTrue(repo.updatedContactos.isEmpty())
    }

    @Test
    fun `retry with same whatsapp reuses unverified contacto instead of creating another`() = runTest {
        val repo = FakeClientRepository(
            existingContactos = listOf(contacto(id = 15, phone = "+5493704123456", verified = false, nombre = "Ana")),
        )

        val result = SaveClientContactoUseCase(repo)(request(phone = "93704123456", nombre = "Ana"))

        assertEquals(AppResult.Success(15L), result)
        assertTrue(repo.createdContactos.isEmpty())
        // Mismo nombre, número, email y favorito: no hace falta PATCH.
        assertTrue(repo.updatedContactos.isEmpty())
    }

    @Test
    fun `corrected number patches favorite unverified contacto`() = runTest {
        val repo = FakeClientRepository(
            existingContactos = listOf(contacto(id = 15, phone = "+5493704123456", verified = false)),
        )

        val result = SaveClientContactoUseCase(repo)(request(phone = "93705219109"))

        assertEquals(AppResult.Success(15L), result)
        assertTrue(repo.createdContactos.isEmpty())
        assertEquals(listOf(15L), repo.updatedContactos.map { it.first })
    }

    @Test
    fun `verified contacto with another number is not overwritten`() = runTest {
        val repo = FakeClientRepository(
            existingContactos = listOf(contacto(id = 3, phone = "+543705219109", verified = true)),
            contactoResult = AppResult.Success(20L),
        )

        val result = SaveClientContactoUseCase(repo)(request(phone = "93704123456"))

        assertEquals(AppResult.Success(20L), result)
        assertEquals(1, repo.createdContactos.size)
        assertTrue(repo.updatedContactos.isEmpty())
    }

    @Test
    fun `patch error on reused contacto is propagated`() = runTest {
        val repo = FakeClientRepository(
            existingContactos = listOf(contacto(id = 15, phone = "+5493704123456", verified = false)),
            updateResult = AppResult.Error(AppError.Validation("Teléfono inválido")),
        )

        val result = SaveClientContactoUseCase(repo)(request(phone = "93705219109"))

        assertTrue(result is AppResult.Error)
        assertTrue(repo.createdContactos.isEmpty())
    }

    @Test
    fun `save domicilio patches existing favorite instead of creating a duplicate`() = runTest {
        val repo = FakeClientRepository(existingDomicilios = listOf(domicilio(id = 9, favorito = true)))

        val result = SaveClientDomicilioUseCase(repo)(ClientDomicilioRequest(barrioId = 1, personaId = 2, calle = "Calle"))

        assertTrue(result is AppResult.Success)
        assertEquals(0, repo.createdDomicilios)
        assertEquals(listOf(9L), repo.updatedDomicilios)
    }

    @Test
    fun `save domicilio creates when there is none`() = runTest {
        val repo = FakeClientRepository()

        SaveClientDomicilioUseCase(repo)(ClientDomicilioRequest(barrioId = 1, personaId = 2, calle = "Calle"))

        assertEquals(1, repo.createdDomicilios)
        assertTrue(repo.updatedDomicilios.isEmpty())
    }

    private fun request(phone: String, nombre: String = "Ana") =
        ClientContactoRequest(nombre = nombre, telefonoWhatsapp = phone, email = null, favorito = true)

    private fun contacto(id: Long, phone: String, verified: Boolean, nombre: String = "Ana") = ClientContacto(
        id = id,
        nombre = nombre,
        telefonoWhatsapp = phone,
        telefonoVerificado = verified,
        email = null,
        favorito = true,
    )

    private fun domicilio(id: Long, favorito: Boolean) = ClientDomicilio(
        id = id,
        barrioId = 1,
        barrioName = "Centro",
        nombre = "Domicilio principal",
        calle = "Calle",
        numero = null,
        casaTorre = null,
        departamento = null,
        piso = null,
        favorito = favorito,
    )

    private class FakeClientRepository(
        private val domicilioResult: AppResult<Unit> = AppResult.Success(Unit),
        private val contactoResult: AppResult<Long> = AppResult.Success(1L),
        private val updateResult: AppResult<Unit> = AppResult.Success(Unit),
        private val existingContactos: List<ClientContacto> = emptyList(),
        private val existingDomicilios: List<ClientDomicilio> = emptyList(),
    ) : ClientProfileRepository {
        val createdContactos = mutableListOf<ClientContactoRequest>()
        val updatedContactos = mutableListOf<Pair<Long, ClientContactoRequest>>()
        var createdDomicilios = 0
        val updatedDomicilios = mutableListOf<Long>()

        override suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit> {
            createdDomicilios++
            return domicilioResult
        }
        override suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long> {
            createdContactos += request
            return contactoResult
        }
        override suspend fun updateDomicilio(id: Long, request: ClientDomicilioRequest): AppResult<Unit> {
            updatedDomicilios += id
            return domicilioResult
        }
        override suspend fun updateContacto(id: Long, request: ClientContactoRequest): AppResult<Unit> {
            updatedContactos += id to request
            return updateResult
        }
        override suspend fun sendPhoneVerificationCode(contactId: Long) =
            AppResult.Success(com.cocido.oficios.domain.model.PhoneVerificationSendResult("ok", null))
        override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
        override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> = AppResult.Success(existingDomicilios)
        override suspend fun getMyContactos(): AppResult<List<ClientContacto>> = AppResult.Success(existingContactos)
    }
}
