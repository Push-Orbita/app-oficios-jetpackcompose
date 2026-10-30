package com.cocido.oficios.feature.onboarding.provider

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AccountStatus
import com.cocido.oficios.domain.model.ClientProfileStatus
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest
import com.cocido.oficios.domain.model.ProviderProfileStatus
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.model.ProviderRubroFullRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft
import com.cocido.oficios.domain.repository.OnboardingRepository
import com.cocido.oficios.domain.repository.ProviderProfileRepository
import com.cocido.oficios.domain.repository.RegisterTradeDraftRepository
import com.cocido.oficios.domain.usecase.auth.GetOnboardingStatusUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitProviderRubroFullUseCase
import com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase
import com.cocido.oficios.domain.usecase.profile.UpdateProviderProfessionalInfoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderPaymentMethodsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `saving payment methods updates profile without recreating primary trade`() = runTest(dispatcher) {
        val draft = sampleDraft()
        val draftRepository = FakeDraftRepository(draft)
        val providerRepository = FakeProviderRepository(draft)
        val viewModel = ProviderPaymentMethodsViewModel(
            updateProviderProfessionalInfoUseCase = UpdateProviderProfessionalInfoUseCase(providerRepository),
            submitProviderRubroFullUseCase = SubmitProviderRubroFullUseCase(providerRepository),
            registerTradeDraftRepository = draftRepository,
            syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                providerRepository,
                draftRepository,
            ),
            getOnboardingStatusUseCase = GetOnboardingStatusUseCase(FakeOnboardingRepository()),
        )
        val effects = mutableListOf<ProviderStepEffect>()
        val effectJob = launch { viewModel.effects.collect(effects::add) }
        advanceUntilIdle()

        viewModel.onMethodToggled(ProviderPaymentMethods.TRANSFERENCIA, true)
        viewModel.submit()
        advanceUntilIdle()

        // Draft sin workDays → no reenvía /full; solo PATCH de pagos.
        assertEquals(0, providerRepository.fullSubmissions)
        assertEquals(
            setOf(ProviderPaymentMethods.EFECTIVO, ProviderPaymentMethods.TRANSFERENCIA),
            providerRepository.lastProfessionalInfo?.paymentMethods?.toSet(),
        )
        assertTrue(effects.any { it is ProviderStepEffect.Saved })
        effectJob.cancel()
    }

    @Test
    fun `successful patch without backend persistence stays on form with professional error`() = runTest(dispatcher) {
        val draft = sampleDraft()
        val draftRepository = FakeDraftRepository(draft)
        val providerRepository = FakeProviderRepository(draft)
        val viewModel = ProviderPaymentMethodsViewModel(
            updateProviderProfessionalInfoUseCase = UpdateProviderProfessionalInfoUseCase(providerRepository),
            submitProviderRubroFullUseCase = SubmitProviderRubroFullUseCase(providerRepository),
            registerTradeDraftRepository = draftRepository,
            syncRegisterTradeDraftFromProfileUseCase = SyncRegisterTradeDraftFromProfileUseCase(
                providerRepository,
                draftRepository,
            ),
            getOnboardingStatusUseCase = GetOnboardingStatusUseCase(
                FakeOnboardingRepository(paymentPersists = false),
            ),
        )
        val effects = mutableListOf<ProviderStepEffect>()
        val effectJob = launch { viewModel.effects.collect(effects::add) }
        advanceUntilIdle()

        viewModel.onMethodToggled(ProviderPaymentMethods.EFECTIVO, true)
        viewModel.submit()
        advanceUntilIdle()

        assertFalse(effects.any { it is ProviderStepEffect.Saved })
        assertTrue(viewModel.uiState.value.error.orEmpty().contains("No pudimos confirmar"))
        assertEquals(setOf(ProviderPaymentMethods.EFECTIVO), viewModel.uiState.value.selectedMethods)
        effectJob.cancel()
    }

    private fun sampleDraft() = RegisterTradeDraft(
        categoryId = 1,
        categoryName = "Hogar",
        rubroId = 10,
        rubroName = "Plomería",
        subRubroIds = listOf(100),
        otherSpecialization = null,
        experienceRange = "3 años",
        neighborhoodQuery = "Centro",
        timeSlots = listOf(RegisterTradeTimeSlotDraft("09:00", "18:00")),
        paymentMethods = listOf(ProviderPaymentMethods.EFECTIVO),
    )

    private class FakeDraftRepository(
        private var value: RegisterTradeDraft?,
    ) : RegisterTradeDraftRepository {
        override suspend fun save(draft: RegisterTradeDraft) { value = draft }
        override suspend fun get(): RegisterTradeDraft? = value
        override suspend fun clear() { value = null }
    }

    private class FakeProviderRepository(
        private val draft: RegisterTradeDraft,
    ) : ProviderProfileRepository {
        var fullSubmissions = 0
        var lastProfessionalInfo: ProviderProfessionalInfoRequest? = null

        override suspend fun updateProfessionalInfo(request: ProviderProfessionalInfoRequest): AppResult<Unit> {
            lastProfessionalInfo = request
            return AppResult.Success(Unit)
        }

        override suspend fun submitRubroFull(request: ProviderRubroFullRequest): AppResult<Unit> {
            fullSubmissions++
            return AppResult.Success(Unit)
        }

        override suspend fun submitSecondaryRubroFull(request: ProviderRubroFullRequest) = AppResult.Success(Unit)
        override suspend fun finalizeProfile() = AppResult.Success(Unit)
        override suspend fun loadProviderProfileForSync() = AppResult.Success(
            ProviderProfileSync(tradeDraft = draft, paymentMethods = draft.paymentMethods),
        )
        override suspend fun getMySubRubros() = AppResult.Success(emptyList<com.cocido.oficios.domain.model.ProviderSubRubroOption>())
    }

    private class FakeOnboardingRepository(
        private val paymentPersists: Boolean = true,
    ) : OnboardingRepository {
        private var providerStatusCalls = 0

        override suspend fun getProviderProfileStatus() = AppResult.Success(
            ProviderProfileStatus(
                accountStatus = AccountStatus.APPROVED,
                isConfigurationComplete = false,
                isProfileComplete = false,
                isOperational = false,
                isIdentityVerified = true,
                // Con paymentPersists el PATCH se considera confirmado en la verificación post-guardado.
                missingSteps = if (paymentPersists) emptyList() else listOf("metodos_pago"),
            ),
        )

        override suspend fun getClientProfileStatus() = AppResult.Success(
            ClientProfileStatus(
                isComplete = false,
                missingSteps = emptyList(),
                hasContact = false,
                hasAddress = false,
                emailVerified = false,
                accountStatus = AccountStatus.INCOMPLETE,
            ),
        )

        override suspend fun getClientRequiredSteps(status: ClientProfileStatus): List<OnboardingStep> = emptyList()
        override suspend fun getProviderRequiredSteps(status: ProviderProfileStatus): List<OnboardingStep> = emptyList()
    }
}
