package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
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
        val draftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
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
        }
        val viewModel = ProviderOnboardingViewModel(
            getOnboardingStatusUseCase = GetOnboardingStatusUseCase(FakeOnboardingRepository()),
            registerTradeDraftRepository = draftRepository,
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
                    registerTradeDraftRepository = draftRepository,
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
            resolveInitialDestinationUseCase = ResolveInitialDestinationUseCase(
                sessionRepository = object : com.cocido.oficios.domain.repository.SessionRepository {
                    override val authState = kotlinx.coroutines.flow.flowOf(
                        com.cocido.oficios.domain.model.AuthState.Unauthenticated,
                    )
                    override suspend fun saveSession(
                        tokens: com.cocido.oficios.domain.model.AuthTokens,
                        user: com.cocido.oficios.domain.model.User,
                    ) = Unit
                    override suspend fun clearSession() = Unit
                    override suspend fun getAccessToken() = "token"
                    override suspend fun getRefreshToken() = null
                    override suspend fun updateTokens(tokens: com.cocido.oficios.domain.model.AuthTokens) = Unit
                    override suspend fun getCachedUser() = null
                },
                authRepository = object : com.cocido.oficios.domain.repository.AuthRepository {
                    override suspend fun login(email: String, password: String) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun registerClient(request: com.cocido.oficios.domain.model.RegisterClientRequest) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun registerProvider(request: com.cocido.oficios.domain.model.RegisterProviderRequest) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown(""))
                    override suspend fun getCurrentUser() = AppResult.Error(AppError.Unknown(""))
                    override suspend fun logout() = AppResult.Success(Unit)
                    override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
                    override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown(""))
                    override suspend fun resetPassword(
                        resetToken: String,
                        password: String,
                        confirmPassword: String,
                    ) = AppResult.Success(Unit)
                    override suspend fun getUserRoles() =
                        AppResult.Success(emptyList<com.cocido.oficios.domain.model.UserRoleDefinition>())
                },
                onboardingRepository = FakeOnboardingRepository(),
            ),
        )
        advanceUntilIdle()

        assertTrue(retryCalled)
        val state = viewModel.uiState.value
        assertTrue(state is ProviderOnboardingUiState.Success)
        assertTrue((state as ProviderOnboardingUiState.Success).steps.isEmpty())
    }

    private class FakeOnboardingRepository : OnboardingRepository {
        private var fetchCount = 0

        override suspend fun getProviderProfileStatus(): AppResult<ProviderProfileStatus> {
            fetchCount++
            return AppResult.Success(
                if (fetchCount == 1) {
                    ProviderProfileStatus(
                        accountStatus = AccountStatus.PENDING_APPROVAL,
                        isConfigurationComplete = false,
                        isProfileComplete = false,
                        isOperational = false,
                        missingSteps = listOf("rubro_principal"),
                    )
                } else {
                    ProviderProfileStatus(
                        accountStatus = AccountStatus.PENDING_APPROVAL,
                        isConfigurationComplete = false,
                        isProfileComplete = false,
                        isOperational = false,
                        missingSteps = emptyList(),
                    )
                },
            )
        }

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

        override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> = emptyList()

        override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> =
            if (status.missingSteps.isEmpty()) {
                emptyList()
            } else {
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
}
