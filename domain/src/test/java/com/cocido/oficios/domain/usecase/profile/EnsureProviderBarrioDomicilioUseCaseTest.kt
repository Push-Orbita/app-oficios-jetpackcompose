package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountDeletionRequest
import com.cocido.oficios.domain.model.AccountProfile
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RequestAccountDeletionParams
import com.cocido.oficios.domain.model.UpdateAccountProfileRequest
import com.cocido.oficios.domain.model.UpdateClientContactoRequest
import com.cocido.oficios.domain.model.UpdateClientDomicilioRequest
import com.cocido.oficios.domain.model.WorkCertificateItem
import com.cocido.oficios.domain.model.WorkGalleryItem
import com.cocido.oficios.domain.repository.AccountRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureProviderBarrioDomicilioUseCaseTest {

    @Test
    fun `creates favorite domicilio with selected barrio when worker has none`() = runTest {
        val repository = FakeAccountRepository(existing = emptyList())

        val result = EnsureProviderBarrioDomicilioUseCase(repository)(barrioId = 42L, barrioName = " San Martín ")

        assertTrue(result is AppResult.Success)
        val created = repository.created.single()
        assertEquals(42L, created.barrioId)
        assertEquals("San Martín", created.nombre)
        assertTrue(created.favorito)
    }

    @Test
    fun `does not duplicate when a domicilio in that barrio already exists`() = runTest {
        val repository = FakeAccountRepository(existing = listOf(domicilio(id = 7L, barrioId = 42L, favorito = true)))

        val result = EnsureProviderBarrioDomicilioUseCase(repository)(barrioId = 42L, barrioName = "San Martín")

        assertTrue(result is AppResult.Success)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `adds non favorite domicilio when another favorite exists`() = runTest {
        val repository = FakeAccountRepository(existing = listOf(domicilio(id = 7L, barrioId = 1L, favorito = true)))

        EnsureProviderBarrioDomicilioUseCase(repository)(barrioId = 42L, barrioName = "San Martín")

        assertFalse(repository.created.single().favorito)
    }

    @Test
    fun `does not create when existing domicilios cannot be read`() = runTest {
        val repository = FakeAccountRepository(
            existing = emptyList(),
            listResult = AppResult.Error(AppError.Validation("offline")),
        )

        val result = EnsureProviderBarrioDomicilioUseCase(repository)(barrioId = 42L, barrioName = "San Martín")

        assertTrue(result is AppResult.Error)
        assertTrue(repository.created.isEmpty())
    }

    private fun domicilio(id: Long, barrioId: Long, favorito: Boolean) = ClientDomicilio(
        id = id,
        barrioId = barrioId,
        barrioName = null,
        nombre = null,
        calle = null,
        numero = null,
        casaTorre = null,
        departamento = null,
        piso = null,
        favorito = favorito,
    )

    private class FakeAccountRepository(
        private val existing: List<ClientDomicilio>,
        private val listResult: AppResult<List<ClientDomicilio>> = AppResult.Success(existing),
    ) : AccountRepository {
        val created = mutableListOf<ClientDomicilioRequest>()

        override suspend fun getClientDomicilios() = listResult
        override suspend fun createClientDomicilio(request: ClientDomicilioRequest): AppResult<Unit> {
            created += request
            return AppResult.Success(Unit)
        }

        override suspend fun getProfile(): AppResult<AccountProfile> = unused()
        override suspend fun updateProfile(request: UpdateAccountProfileRequest): AppResult<AccountProfile> = unused()
        override suspend fun changePassword(email: String, password: String, confirmation: String): AppResult<Unit> = unused()
        override suspend fun updateClientDomicilio(id: Long, request: UpdateClientDomicilioRequest): AppResult<Unit> = unused()
        override suspend fun deleteClientDomicilio(id: Long): AppResult<Unit> = unused()
        override suspend fun setFavoriteDomicilio(id: Long, favorite: Boolean): AppResult<Unit> = unused()
        override suspend fun getClientContactos(): AppResult<List<ClientContacto>> = unused()
        override suspend fun createClientContacto(request: ClientContactoRequest): AppResult<Long> = unused()
        override suspend fun updateClientContacto(id: Long, request: UpdateClientContactoRequest): AppResult<Unit> = unused()
        override suspend fun deleteClientContacto(id: Long): AppResult<Unit> = unused()
        override suspend fun setFavoriteContacto(id: Long, favorite: Boolean): AppResult<Unit> = unused()
        override suspend fun updateProviderProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit> = unused()
        override suspend fun addProviderService(request: ProviderRubroFullRequest): AppResult<Unit> = unused()
        override suspend fun deleteProviderService(id: Long): AppResult<Unit> = unused()
        override suspend fun replaceProviderService(
            existingId: Long,
            isPrimary: Boolean,
            request: ProviderRubroFullRequest,
        ): AppResult<Unit> = unused()
        override suspend fun getCertificates(): AppResult<List<WorkCertificateItem>> = unused()
        override suspend fun uploadCertificate(name: String, filePath: String): AppResult<WorkCertificateItem> = unused()
        override suspend fun updateCertificate(id: Long, name: String, currentUrl: String?, filePath: String?): AppResult<Unit> = unused()
        override suspend fun deleteCertificate(id: Long): AppResult<Unit> = unused()
        override suspend fun getGallery(): AppResult<List<WorkGalleryItem>> = unused()
        override suspend fun uploadGallery(filePaths: List<String>): AppResult<List<WorkGalleryItem>> = unused()
        override suspend fun deleteGalleryItem(id: Long): AppResult<Unit> = unused()
        override suspend fun getCurrentDeletionRequest(): AppResult<AccountDeletionRequest?> = unused()
        override suspend fun requestAccountDeletion(params: RequestAccountDeletionParams): AppResult<AccountDeletionRequest> = unused()
        override suspend fun cancelAccountDeletion(): AppResult<AccountDeletionRequest> = unused()

        private fun unused(): Nothing = error("not used")
    }
}