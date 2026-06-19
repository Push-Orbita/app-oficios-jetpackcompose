package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class FinalizeProviderProfileUseCaseTest {

    @Test
    fun `propagates success`() = runTest {
        val useCase = FinalizeProviderProfileUseCase(FakeProviderRepository(AppResult.Success(Unit)))
        assertTrue(useCase() is AppResult.Success)
    }

    @Test
    fun `propagates error`() = runTest {
        val useCase = FinalizeProviderProfileUseCase(
            FakeProviderRepository(AppResult.Error(AppError.Network("Error del servidor"))),
        )
        assertTrue(useCase() is AppResult.Error)
    }

    private class FakeProviderRepository(
        private val finalizeResult: AppResult<Unit>,
    ) : ProviderProfileRepository {
        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
            AppResult.Success(Unit)

        override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
            AppResult.Success(Unit)

        override suspend fun finalizeProfile() = finalizeResult

        override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
    }
}
