package com.cocido.oficios.feature.auth.register

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ProviderProfileSync
import com.cocido.oficios.domain.usecase.auth.CompleteProviderRegistrationUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterClientUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterProviderUseCase
import com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase
import com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase
import com.cocido.oficios.feature.auth.FakeAuthRepository
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
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        draftSubmitted: AtomicBoolean = AtomicBoolean(false),
        rubroSyncResult: AppResult<Unit> = AppResult.Success(Unit),
    ) = RegisterViewModel(
        RegisterClientUseCase(FakeAuthRepository()),
        CompleteProviderRegistrationUseCase(
            registerProviderUseCase = RegisterProviderUseCase(FakeAuthRepository()),
            submitRegisterTradeDraftUseCase = SubmitRegisterTradeDraftUseCase(
                providerProfileRepository = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
                    override suspend fun updateProfessionalInfo(
                        request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
                    ) = AppResult.Success(Unit).also { draftSubmitted.set(true) }
                    override suspend fun submitRubroFull(
                        request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
                    ) = rubroSyncResult
                    override suspend fun finalizeProfile() = AppResult.Success(Unit)
                    override suspend fun loadProviderProfileForSync() = AppResult.Success(ProviderProfileSync(tradeDraft = null))
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
                    override suspend fun getCategories(): AppResult<List<CatalogOption>> =
                        AppResult.Success(emptyList())
                    override suspend fun getRubrosByCategory(categoryId: Long): AppResult<List<CatalogOption>> =
                        AppResult.Success(emptyList())
                    override suspend fun getSubRubrosByRubro(rubroId: Long): AppResult<List<CatalogOption>> =
                        AppResult.Success(emptyList())
                    override suspend fun searchBarrios(query: String): AppResult<List<CatalogOption>> =
                        AppResult.Success(emptyList())
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
    )

    @Test
    fun `password mismatch blocks submit`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("different"))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `invalid email blocks submit`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        viewModel.onEvent(RegisterFormEvent.EmailChanged("invalid"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `client registration success`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.CLIENT)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `provider registration requires document`() {
        val viewModel = createViewModel()
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.DocumentChanged(""))
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `provider registration syncs rubro to backend before finishing`() = runTest(dispatcher) {
        val draftSubmitted = AtomicBoolean(false)
        val viewModel = createViewModel(draftSubmitted)
        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(draftSubmitted.get())
    }

    @Test
    fun `provider registration continues to onboarding when rubro sync fails`() = runTest(dispatcher) {
        val viewModel = createViewModel(
            rubroSyncResult = AppResult.Error(AppError.Network(message = "Error del servidor")),
        )
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effects.collect { effects.add(it) } }

        viewModel.setMode(RegisterMode.PROVIDER)
        fillValidClientForm(viewModel)
        viewModel.onEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        assertEquals("Error del servidor", viewModel.uiState.value.generalError)
        assertTrue(effects.any { it is RegisterEffect.NavigateToOnboarding })
        job.cancel()
    }

    private fun fillValidClientForm(viewModel: RegisterViewModel) {
        viewModel.onEvent(RegisterFormEvent.FullNameChanged("Juan Pérez"))
        viewModel.onEvent(RegisterFormEvent.DocumentChanged("12345678"))
        viewModel.onEvent(RegisterFormEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
        viewModel.onEvent(RegisterFormEvent.TermsChanged(true))
    }
}
