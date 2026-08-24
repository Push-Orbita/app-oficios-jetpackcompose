package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitRegisterTradeDraftUseCaseTest {

    @Test
    fun `keeps draft when rubro submission fails`() = runTest {
        val draftRepository = FakeDraftRepository(
            draft = sampleDraft(),
        )
        val providerRepository = FakeProviderRepository(
            rubroResult = AppResult.Error(AppError.Validation("DNI pendiente")),
        )
        val useCase = createUseCase(draftRepository, providerRepository)

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertTrue(draftRepository.get() != null)
    }

    @Test
    fun `keeps draft on full success for onboarding edits`() = runTest {
        val draft = sampleDraft()
        val draftRepository = FakeDraftRepository(draft = draft)
        val useCase = createUseCase(draftRepository)

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(draft, draftRepository.get())
    }

    @Test
    fun `resolves barrioId from catalog search`() = runTest {
        val draftRepository = FakeDraftRepository(draft = sampleDraft())
        val catalogRepository = FakeCatalogRepository(
            barrios = listOf(CatalogOption(id = 99, label = "Centro")),
        )
        val useCase = SubmitRegisterTradeDraftUseCase(
            providerProfileRepository = FakeProviderRepository(
                rubroResult = AppResult.Error(AppError.Validation("DNI pendiente")),
            ),
            registerTradeDraftRepository = draftRepository,
            catalogRepository = catalogRepository,
            syncRegisterTradeDraftFromProfileUseCase = createSyncUseCase(draftRepository),
        )

        useCase()

        assertEquals(99L, draftRepository.get()?.barrioId)
    }

    @Test
    fun `loads draft from profile when local draft is missing`() = runTest {
        val remoteDraft = sampleDraft()
        val draftRepository = FakeDraftRepository(draft = null)
        val useCase = SubmitRegisterTradeDraftUseCase(
            providerProfileRepository = FakeProviderRepository(
                tradeDraft = remoteDraft,
            ),
            registerTradeDraftRepository = draftRepository,
            catalogRepository = FakeCatalogRepository(),
            syncRegisterTradeDraftFromProfileUseCase = createSyncUseCase(
                draftRepository,
                FakeProviderRepository(tradeDraft = remoteDraft),
            ),
        )

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(remoteDraft, draftRepository.get())
    }

    @Test
    fun `requireDraft fails when draft is missing`() = runTest {
        val draftRepository = FakeDraftRepository(draft = null)
        val useCase = createUseCase(draftRepository)

        val result = useCase(requireDraft = true)

        assertTrue(result is AppResult.Error)
    }

    private fun createUseCase(
        draftRepository: FakeDraftRepository,
        providerRepository: FakeProviderRepository = FakeProviderRepository(),
    ) = SubmitRegisterTradeDraftUseCase(
        providerProfileRepository = providerRepository,
        registerTradeDraftRepository = draftRepository,
        catalogRepository = FakeCatalogRepository(),
        syncRegisterTradeDraftFromProfileUseCase = createSyncUseCase(draftRepository, providerRepository),
    )

    private fun createSyncUseCase(
        draftRepository: RegisterTradeDraftRepository,
        providerRepository: ProviderProfileRepository = FakeProviderRepository(),
    ) = SyncRegisterTradeDraftFromProfileUseCase(
        providerProfileRepository = providerRepository,
        registerTradeDraftRepository = draftRepository,
    )

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
        private val rubroResult: AppResult<Unit> = AppResult.Success(Unit),
        private val tradeDraft: RegisterTradeDraft? = null,
    ) : ProviderProfileRepository {
        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest) =
            AppResult.Success(Unit)

        override suspend fun submitRubroFull(request: ProviderRubroFullRequest) = rubroResult

        override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) = rubroResult

        override suspend fun finalizeProfile() = AppResult.Success(Unit)

        override suspend fun loadProviderProfileForSync(): AppResult<ProviderProfileSync> =
            AppResult.Success(ProviderProfileSync(tradeDraft = tradeDraft, paymentMethods = tradeDraft?.paymentMethods.orEmpty()))

        override suspend fun getMySubRubros() = AppResult.Success(emptyList<com.cocido.oficios.domain.model.ProviderSubRubroOption>())
    }

    private class FakeCatalogRepository(
        private val barrios: List<CatalogOption> = emptyList(),
    ) : CatalogRepository {
        override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun searchBarrios(query: String) = AppResult.Success(barrios)
    }
}
