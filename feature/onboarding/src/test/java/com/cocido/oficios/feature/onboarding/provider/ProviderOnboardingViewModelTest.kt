package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.RetryPendingTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderOnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `retries pending draft when rubro is missing`() = runTest(dispatcher) {
        var retryCalled = false
        val viewModel = ProviderOnboardingViewModel(
            getOnboardingStatusUseCase = GetOnboardingStatusUseCase(FakeOnboardingRepository()),
            retryPendingTradeDraftUseCase = RetryPendingTradeDraftUseCase(
                SubmitRegisterTradeDraftUseCase(
                    providerProfileRepository = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
                        override suspend fun updateProfessionalInfo(
                            request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
                        ) = AppResult.Success(Unit).also { retryCalled = true }
                        override suspend fun submitRubroFull(
                            request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
                        ) = AppResult.Success(Unit)
                        override suspend fun finalizeProfile() = AppResult.Success(Unit)
                    },
                    registerTradeDraftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                        override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                        override suspend fun get() = com.cocido.oficios.domain.model.RegisterTradeDraft(
                            categoryId = 1,
                            categoryName = "Hogar",
                            rubroId = 10,
                            rubroName = "Plomería",
                            subRubroIds = listOf(100),
                            otherSpecialization = null,
                            experienceRange = "0 a 2 años",
                            neighborhoodQuery = "Centro",
                            timeSlots = listOf(
                                com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft("09:00", "18:00"),
                            ),
                        )
                        override suspend fun clear() = Unit
                    },
                    catalogRepository = object : com.cocido.oficios.domain.repository.CatalogRepository {
                        override suspend fun getCategories() =
                            AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
                        override suspend fun getRubrosByCategory(categoryId: Long) =
                            AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
                        override suspend fun getSubRubrosByRubro(rubroId: Long) =
                            AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
                        override suspend fun searchBarrios(query: String) =
                            AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
                    },
                ),
            ),
            finalizeProviderProfileUseCase = FinalizeProviderProfileUseCase(
                object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
                    override suspend fun updateProfessionalInfo(
                        request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
                    ) = AppResult.Success(Unit)
                    override suspend fun submitRubroFull(
                        request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
                    ) = AppResult.Success(Unit)
                    override suspend fun finalizeProfile() = AppResult.Success(Unit)
                },
            ),
        )
        advanceUntilIdle()

        assertTrue(retryCalled)
        assertTrue(viewModel.uiState.value is ProviderOnboardingUiState.Success)
    }

    private class FakeOnboardingRepository : OnboardingRepository {
        private val providerStatus = ProviderProfileStatus(
            accountStatus = AccountStatus.PENDING_APPROVAL,
            isConfigurationComplete = false,
            isProfileComplete = false,
            isOperational = false,
            missingSteps = listOf("rubro_principal"),
        )

        override suspend fun getClientProfileStatus() = AppResult.Success(
            ClientProfileStatus(
                isComplete = false,
                missingSteps = emptyList(),
                hasContact = false,
                hasAddress = false,
                emailVerified = false,
                accountStatus = AccountStatus.ACTIVE,
            ),
        )

        override suspend fun getProviderProfileStatus() = AppResult.Success(providerStatus)

        override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> = emptyList()

        override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> =
            listOf(
                OnboardingStep(
                    id = "rubro_principal",
                    title = "Rubro",
                    description = "Seleccionar",
                    status = OnboardingStepStatus.PENDING,
                ),
            )
    }
}
