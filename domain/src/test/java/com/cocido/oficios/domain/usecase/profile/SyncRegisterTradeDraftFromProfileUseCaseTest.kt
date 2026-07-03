package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncRegisterTradeDraftFromProfileUseCaseTest {

    @Test
    fun `saves remote draft when local is missing`() = runTest {
        val remoteDraft = sampleDraft()
        val repository = FakeDraftRepository(draft = null)
        val useCase = SyncRegisterTradeDraftFromProfileUseCase(
            providerProfileRepository = FakeProviderRepository(remoteDraft),
            registerTradeDraftRepository = repository,
        )

        val result = useCase()

        assertEquals(remoteDraft, result)
        assertEquals(remoteDraft, repository.get())
    }

    @Test
    fun `keeps local draft when remote is empty`() = runTest {
        val localDraft = sampleDraft()
        val repository = FakeDraftRepository(draft = localDraft)
        val useCase = SyncRegisterTradeDraftFromProfileUseCase(
            providerProfileRepository = FakeProviderRepository(remoteDraft = null),
            registerTradeDraftRepository = repository,
        )

        val result = useCase()

        assertEquals(localDraft, result)
    }

    @Test
    fun `merges payment methods from profile when rubro draft is missing`() = runTest {
        val localDraft = sampleDraft()
        val repository = FakeDraftRepository(draft = localDraft)
        val useCase = SyncRegisterTradeDraftFromProfileUseCase(
            providerProfileRepository = FakeProviderRepository(
                remoteDraft = null,
                paymentMethods = listOf("EFECTIVO", "TRANSFERENCIA"),
            ),
            registerTradeDraftRepository = repository,
        )

        val result = useCase()

        assertEquals(listOf("EFECTIVO", "TRANSFERENCIA"), result?.paymentMethods)
    }

    @Test
    fun `returns null when both are missing`() = runTest {
        val repository = FakeDraftRepository(draft = null)
        val useCase = SyncRegisterTradeDraftFromProfileUseCase(
            providerProfileRepository = FakeProviderRepository(remoteDraft = null),
            registerTradeDraftRepository = repository,
        )

        assertNull(useCase())
    }

    private fun sampleDraft() = RegisterTradeDraft(
        categoryId = 1,
        categoryName = "Hogar",
        rubroId = 11,
        rubroName = "Plomería",
        subRubroIds = listOf(1),
        otherSpecialization = null,
        experienceRange = "3 a 5 años",
        neighborhoodQuery = "Centro",
        timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
    )

    private class FakeDraftRepository(
        private var draft: RegisterTradeDraft?,
    ) : RegisterTradeDraftRepository {
        override suspend fun save(draft: RegisterTradeDraft) {
            this.draft = draft
        }

        override suspend fun get(): RegisterTradeDraft? = draft

        override suspend fun clear() {
            draft = null
        }
    }

    private class FakeProviderRepository(
        private val remoteDraft: RegisterTradeDraft?,
        private val paymentMethods: List<String> = remoteDraft?.paymentMethods.orEmpty(),
    ) : ProviderProfileRepository {
        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
            AppResult.Success(Unit)

        override suspend fun submitRubroFull(request: ProviderRubroFullRequest) =
            AppResult.Success(Unit)

        override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) =
            AppResult.Success(Unit)

        override suspend fun finalizeProfile() = AppResult.Success(Unit)

        override suspend fun loadProviderProfileForSync(): AppResult<ProviderProfileSync> =
            AppResult.Success(
                ProviderProfileSync(
                    tradeDraft = remoteDraft,
                    paymentMethods = paymentMethods,
                ),
            )
    }
}
