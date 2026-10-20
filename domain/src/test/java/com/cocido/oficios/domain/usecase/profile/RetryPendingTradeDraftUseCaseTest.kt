package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPendingTradeDraftUseCaseTest {

    @Test
    fun `delegates to submit register trade draft use case`() = runTest {
        val draft = RegisterTradeDraft(
            categoryId = 1,
            categoryName = "Hogar",
            rubroId = 10,
            rubroName = "Plomería",
            subRubroIds = listOf(100),
            otherSpecialization = null,
            experienceRange = "0 a 2 años",
            neighborhoodQuery = "Centro",
            timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        )
        val draftRepository = object : RegisterTradeDraftRepository {
            override suspend fun save(draft: RegisterTradeDraft) = Unit
            override suspend fun get() = draft
            override suspend fun clear() = Unit
        }
        val providerProfileRepository = object : ProviderProfileRepository {
            override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
                AppResult.Success(Unit)

            override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
                AppResult.Success(Unit)

            override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) =
                AppResult.Success(Unit)

            override suspend fun finalizeProfile() = AppResult.Success(Unit)
            override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
            override suspend fun getMySubRubros() = AppResult.Success(emptyList<ProviderSubRubroOption>())
        }
        val submitUseCase = SubmitRegisterTradeDraftUseCase(
            providerProfileRepository = providerProfileRepository,
            registerTradeDraftRepository = draftRepository,
            catalogRepository = object : CatalogRepository {
                override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
                override suspend fun getRubrosByCategory(categoryId: Long) =
                    AppResult.Success(emptyList<CatalogOption>())
                override suspend fun getSubRubrosByRubro(rubroId: Long) =
                    AppResult.Success(emptyList<CatalogOption>())
                override suspend fun searchBarrios(query: String) =
                    AppResult.Success(emptyList<CatalogOption>())
            },
            syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                providerProfileRepository = providerProfileRepository,
                registerTradeDraftRepository = draftRepository,
            ),
        )
        val useCase = RetryPendingTradeDraftUseCase(submitUseCase)

        assertTrue(useCase() is AppResult.Success)
    }
}
