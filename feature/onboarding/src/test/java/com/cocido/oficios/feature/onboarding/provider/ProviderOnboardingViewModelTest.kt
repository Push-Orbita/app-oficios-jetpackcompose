package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.InitialDestination
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.auth.ResolveInitialDestinationUseCase
import com.cocido.oficios.domain.usecase.profile.FinalizeProviderProfileUseCase
import com.cocido.oficios.domain.usecase.profile.RetryPendingTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
        val providerProfileRepository = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
            override suspend fun updateProfessionalInfo(
                request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
            ) = AppResult.Success(Unit).also { retryCalled = true }
            override suspend fun submitRubroFull(
                request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
            ) = AppResult.Success(Unit)
            override suspend fun finalizeProfile() = AppResult.Success(Unit)
            override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
        }
        val viewModel = ProviderOnboardingViewModel(
            getOnboardingStatusUseCase = GetOnboardingStatusUseCase(FakeOnboardingRepository()),
            registerTradeDraftRepository = draftRepository,
            syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                providerProfileRepository = providerProfileRepository,
                registerTradeDraftRepository = draftRepository,
            ),
            retryPendingTradeDraftUseCase = RetryPendingTradeDraftUseCase(
                SubmitRegisterTradeDraftUseCase(
                    providerProfileRepository = providerProfileRepository,
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
                    syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                        providerProfileRepository = providerProfileRepository,
                        registerTradeDraftRepository = draftRepository,
                    ),
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
                    override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
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
                    override suspend fun updateCachedUser(user: com.cocido.oficios.domain.model.User) = Unit
                },
                authRepository = object : com.cocido.oficios.domain.repository.AuthRepository {
                    override suspend fun login(email: String, password: String) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun registerClient(request: com.cocido.oficios.domain.model.RegisterClientRequest) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun registerProvider(request: com.cocido.oficios.domain.model.RegisterProviderRequest) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown(""))
                    override suspend fun getCurrentUser() = AppResult.Success(
                        User(1, "user@test.com", "Juan", "Pérez", UserRole.PROVIDER, true),
                    )
                    override suspend fun logout() = AppResult.Success(Unit)
                    override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
                    override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown(""))
                    override suspend fun resetPassword(
                        resetToken: String,
                        password: String,
                        confirmPassword: String,
                    ) = AppResult.Success(Unit)
                    override suspend fun sendVerificationEmail() = AppResult.Success("Código de verificación reenviado a tu correo")
                    override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
                    override suspend fun getUserRoles() =
                        AppResult.Success(emptyList<com.cocido.oficios.domain.model.UserRoleDefinition>())
                },
                onboardingRepository = object : OnboardingRepository {
                    override suspend fun getProviderProfileStatus() = AppResult.Success(
                        ProviderProfileStatus(
                            accountStatus = AccountStatus.PENDING_APPROVAL,
                            isConfigurationComplete = true,
                            isProfileComplete = true,
                            isOperational = false,
                            isIdentityVerified = true,
                            missingSteps = emptyList(),
                        ),
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
                    override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> =
                        emptyList()
                    override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> =
                        emptyList()
                },
            ),
        )
        val navigation = launch {
            viewModel.effects.collect { effect ->
                if (effect is com.cocido.oficios.feature.onboarding.common.OnboardingChecklistEffect.NavigateAfterCompletion) {
                    assertEquals(InitialDestination.ProviderPendingApproval, effect.destination)
                }
            }
        }
        advanceUntilIdle()

        assertTrue(retryCalled)
        navigation.cancel()
    }

    @Test
    fun `auto finalizes when only finalize step remains`() = runTest(dispatcher) {
        var finalizeCalled = false
        val viewModel = ProviderOnboardingViewModel(
            getOnboardingStatusUseCase = GetOnboardingStatusUseCase(
                object : OnboardingRepository {
                    override suspend fun getProviderProfileStatus() = AppResult.Success(
                        ProviderProfileStatus(
                            accountStatus = AccountStatus.PENDING_APPROVAL,
                            isConfigurationComplete = true,
                            isProfileComplete = false,
                            isOperational = false,
                            isIdentityVerified = true,
                            missingSteps = listOf("finalizar_perfil"),
                        ),
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
                    override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> =
                        emptyList<OnboardingStep>()
                    override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> =
                        listOf(
                            OnboardingStep(
                                id = "finalizar_perfil",
                                title = "Finalizar perfil",
                                description = "Confirmá",
                                status = OnboardingStepStatus.PENDING,
                            ),
                        )
                },
            ),
            registerTradeDraftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                override suspend fun get() = null
                override suspend fun clear() = Unit
            },
            syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                providerProfileRepository = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
                    override suspend fun updateProfessionalInfo(
                        request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
                    ) = AppResult.Success(Unit)
                    override suspend fun submitRubroFull(
                        request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
                    ) = AppResult.Success(Unit)
                    override suspend fun finalizeProfile() = AppResult.Success(Unit).also { finalizeCalled = true }
                    override suspend fun loadProviderProfileForSync() =
                        AppResult.Success(ProviderProfileSync(tradeDraft = null))
                },
                registerTradeDraftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                    override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                    override suspend fun get() = null
                    override suspend fun clear() = Unit
                },
            ),
            retryPendingTradeDraftUseCase = RetryPendingTradeDraftUseCase(
                SubmitRegisterTradeDraftUseCase(
                    providerProfileRepository = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
                        override suspend fun updateProfessionalInfo(
                            request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
                        ) = AppResult.Success(Unit)
                        override suspend fun submitRubroFull(
                            request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
                        ) = AppResult.Success(Unit)
                        override suspend fun finalizeProfile() = AppResult.Success(Unit)
                        override suspend fun loadProviderProfileForSync() =
                            AppResult.Success(ProviderProfileSync(tradeDraft = null))
                    },
                    registerTradeDraftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                        override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                        override suspend fun get() = null
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
                    syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                        providerProfileRepository = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
                            override suspend fun updateProfessionalInfo(
                                request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
                            ) = AppResult.Success(Unit)
                            override suspend fun submitRubroFull(
                                request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
                            ) = AppResult.Success(Unit)
                            override suspend fun finalizeProfile() = AppResult.Success(Unit)
                            override suspend fun loadProviderProfileForSync() =
                                AppResult.Success(ProviderProfileSync(tradeDraft = null))
                        },
                        registerTradeDraftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                            override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                            override suspend fun get() = null
                            override suspend fun clear() = Unit
                        },
                    ),
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
                    override suspend fun finalizeProfile() = AppResult.Success(Unit).also { finalizeCalled = true }
                    override suspend fun loadProviderProfileForSync() =
                        AppResult.Success(ProviderProfileSync(tradeDraft = null))
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
                    override suspend fun updateCachedUser(user: com.cocido.oficios.domain.model.User) = Unit
                },
                authRepository = object : com.cocido.oficios.domain.repository.AuthRepository {
                    override suspend fun login(email: String, password: String) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun registerClient(request: com.cocido.oficios.domain.model.RegisterClientRequest) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun registerProvider(request: com.cocido.oficios.domain.model.RegisterProviderRequest) =
                        AppResult.Error(AppError.Unknown(""))
                    override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown(""))
                    override suspend fun getCurrentUser() = AppResult.Success(
                        User(1, "user@test.com", "Juan", "Pérez", UserRole.PROVIDER, true),
                    )
                    override suspend fun logout() = AppResult.Success(Unit)
                    override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
                    override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown(""))
                    override suspend fun resetPassword(
                        resetToken: String,
                        password: String,
                        confirmPassword: String,
                    ) = AppResult.Success(Unit)
                    override suspend fun sendVerificationEmail() = AppResult.Success("ok")
                    override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
                    override suspend fun getUserRoles() =
                        AppResult.Success(emptyList<com.cocido.oficios.domain.model.UserRoleDefinition>())
                },
                onboardingRepository = object : OnboardingRepository {
                    override suspend fun getProviderProfileStatus() = AppResult.Success(
                        ProviderProfileStatus(
                            accountStatus = AccountStatus.PENDING_APPROVAL,
                            isConfigurationComplete = true,
                            isProfileComplete = true,
                            isOperational = false,
                            isIdentityVerified = true,
                            missingSteps = emptyList(),
                        ),
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
                    override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> =
                        emptyList<OnboardingStep>()
                    override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> =
                        emptyList<OnboardingStep>()
                },
            ),
        )
        val navigation = launch {
            viewModel.effects.collect { effect ->
                if (effect is com.cocido.oficios.feature.onboarding.common.OnboardingChecklistEffect.NavigateAfterCompletion) {
                    assertEquals(InitialDestination.ProviderPendingApproval, effect.destination)
                }
            }
        }

        advanceUntilIdle()
        assertTrue(finalizeCalled)
        navigation.cancel()
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
                        isIdentityVerified = true,
                        missingSteps = listOf("rubro_principal"),
                    )
                } else {
                    ProviderProfileStatus(
                        accountStatus = AccountStatus.PENDING_APPROVAL,
                        isConfigurationComplete = false,
                        isProfileComplete = false,
                        isOperational = false,
                        isIdentityVerified = true,
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
