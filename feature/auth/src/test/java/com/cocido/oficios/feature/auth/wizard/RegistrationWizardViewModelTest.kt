package com.cocido.oficios.feature.auth.wizard

import androidx.lifecycle.SavedStateHandle
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.PhoneVerificationSendResult
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.RegisterTradeDraft
import com.cocido.oficios.domain.model.RegistrationDraft
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.domain.model.RegistrationPersonalDraft
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.CatalogRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.repository.RegistrationDraftRepository
import com.cocido.oficios.domain.repository.SessionRepository
import com.cocido.oficios.domain.usecase.auth.RegisterClientUseCase
import com.cocido.oficios.domain.usecase.auth.RegisterProviderUseCase
import com.cocido.oficios.domain.usecase.auth.RegistrationCommitter
import com.cocido.oficios.domain.usecase.catalog.SearchBarriosUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import com.cocido.oficios.feature.auth.register.RegisterFormEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationWizardViewModelTest {

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
    fun `personal submit does not create the account`() = runTest(dispatcher) {
        val env = createEnv()
        fillPersonal(env.viewModel)
        env.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()

        assertEquals(0, env.auth.clientAttempts)
        assertTrue(env.effects.contains(RegistrationWizardEffect.GoToProfile))
        val saved = env.drafts.draft
        assertTrue(saved != null)
        assertEquals("ana@test.com", saved?.personal?.email)
        assertFalse(env.drafts.encodedHasPassword)
    }

    @Test
    fun `provider personal submit goes to oficio instead of client profile`() = runTest(dispatcher) {
        val env = createEnv()
        val providerVm = RegistrationWizardViewModel(
            savedStateHandle = SavedStateHandle(mapOf("role" to "PROVIDER")),
            draftRepository = env.drafts,
            committer = RegistrationCommitter(
                registerClientUseCase = RegisterClientUseCase(env.auth),
                registerProviderUseCase = RegisterProviderUseCase(env.auth),
                saveClientContactoUseCase = SaveClientContactoUseCase(FakeClientProfiles()),
                saveClientDomicilioUseCase = SaveClientDomicilioUseCase(FakeClientProfiles()),
                draftRepository = env.drafts,
                registerTradeDraftRepository = object :
                    com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                    override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                    override suspend fun get() = null
                    override suspend fun clear() = Unit
                },
                submitRegisterTradeDraftUseCase = noopTradeSubmit(),
                sessionRepository = FakeSession(),
            ),
            searchBarriosUseCase = SearchBarriosUseCase(FakeCatalog()),
            passwordHolder = RegistrationPasswordHolder(),
        )
        val effects = mutableListOf<RegistrationWizardEffect>()
        val job = kotlinx.coroutines.CoroutineScope(dispatcher).launch {
            providerVm.effects.collect { effects += it }
        }
        fillPersonal(providerVm)
        providerVm.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertTrue(effects.contains(RegistrationWizardEffect.GoToOficio))
        assertFalse(effects.contains(RegistrationWizardEffect.GoToProfile))
        assertEquals(0, env.auth.clientAttempts)
        job.cancel()
    }

    @Test
    fun `create account is the only signup call`() = runTest(dispatcher) {
        val env = createEnv()
        fillPersonal(env.viewModel)
        env.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        fillProfile(env.viewModel)
        env.viewModel.continueFromProfile()
        env.viewModel.createAccount()
        advanceUntilIdle()

        assertEquals(1, env.auth.clientAttempts)
        assertTrue(env.effects.contains(RegistrationWizardEffect.AccountCreated))
        assertNull(env.drafts.draft)
    }

    @Test
    fun `409 returns to personal with draft intact`() = runTest(dispatcher) {
        val env = createEnv(
            registerResult = AppResult.Error(AppError.Conflict("Este correo ya está registrado")),
        )
        fillPersonal(env.viewModel)
        fillProfile(env.viewModel)
        env.viewModel.createAccount()
        advanceUntilIdle()

        assertTrue(env.effects.contains(RegistrationWizardEffect.GoToPersonal))
        assertEquals("Este correo ya está registrado", env.viewModel.uiState.value.personal.emailError)
        assertEquals("ana@test.com", env.drafts.draft?.personal?.email)
        assertFalse(env.drafts.encodedHasPassword)
    }

    @Test
    fun `restores draft fields but never a password from disk`() = runTest(dispatcher) {
        val drafts = InMemoryDrafts(
            RegistrationDraft(
                personal = RegistrationPersonalDraft(
                    firstName = "Ana",
                    lastName = "López",
                    email = "ana@test.com",
                    document = "12345678",
                    acceptedTerms = true,
                ),
            ),
        )
        val env = createEnv(drafts = drafts)
        advanceUntilIdle()

        val personal = env.viewModel.uiState.value.personal
        assertEquals("Ana", personal.firstName)
        assertEquals("ana@test.com", personal.email)
        assertEquals("", personal.password)
        assertEquals("", personal.confirmPassword)
    }

    @Test
    fun `same role in same process restores password from process memory`() = runTest(dispatcher) {
        val holder = RegistrationPasswordHolder()
        val first = createEnv(passwordHolder = holder)
        fillPersonal(first.viewModel)
        first.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        first.collectJob?.cancel()

        val recreated = createEnv(drafts = first.drafts, passwordHolder = holder)
        advanceUntilIdle()
        assertEquals("secret12!", recreated.viewModel.uiState.value.personal.password)
        assertEquals("secret12!", recreated.viewModel.uiState.value.personal.confirmPassword)
        assertFalse(recreated.drafts.encodedHasPassword)
        recreated.collectJob?.cancel()
    }

    @Test
    fun `changing role keeps shared personal fields from draft`() = runTest(dispatcher) {
        val drafts = InMemoryDrafts(
            RegistrationDraft(
                role = RegistrationRole.CLIENT,
                personal = RegistrationPersonalDraft(
                    firstName = "Ana",
                    lastName = "López",
                    email = "ana@test.com",
                    document = "12345678",
                    acceptedTerms = true,
                ),
                client = com.cocido.oficios.domain.model.RegistrationClientDraft(
                    contactName = "Ana López",
                    phone = "93705219109",
                    barrioQuery = "Centro",
                    barrioId = 4,
                    calle = "Belgrano",
                ),
            ),
        )
        val providerVm = RegistrationWizardViewModel(
            savedStateHandle = SavedStateHandle(mapOf("role" to "PROVIDER")),
            draftRepository = drafts,
            committer = RegistrationCommitter(
                registerClientUseCase = RegisterClientUseCase(FakeAuth(AppResult.Error(AppError.Unknown()))),
                registerProviderUseCase = RegisterProviderUseCase(FakeAuth(AppResult.Error(AppError.Unknown()))),
                saveClientContactoUseCase = SaveClientContactoUseCase(FakeClientProfiles()),
                saveClientDomicilioUseCase = SaveClientDomicilioUseCase(FakeClientProfiles()),
                draftRepository = drafts,
                registerTradeDraftRepository = object :
                    com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                    override suspend fun save(draft: RegisterTradeDraft) = Unit
                    override suspend fun get() = null
                    override suspend fun clear() = Unit
                },
                submitRegisterTradeDraftUseCase = noopTradeSubmit(),
                sessionRepository = FakeSession(),
            ),
            searchBarriosUseCase = SearchBarriosUseCase(FakeCatalog()),
            passwordHolder = RegistrationPasswordHolder(),
        )
        advanceUntilIdle()
        val personal = providerVm.uiState.value.personal
        assertEquals("Ana", personal.firstName)
        assertEquals("López", personal.lastName)
        assertEquals("ana@test.com", personal.email)
        assertEquals("12345678", personal.document)
        assertTrue(personal.acceptedTerms)
        assertEquals("", providerVm.uiState.value.contactName)
        assertEquals("", providerVm.uiState.value.phone)
    }

    @Test
    fun `terms validation uses marker not hardcoded copy`() = runTest(dispatcher) {
        val env = createEnv()
        env.viewModel.onPersonalEvent(RegisterFormEvent.FirstNameChanged("Ana"))
        env.viewModel.onPersonalEvent(RegisterFormEvent.LastNameChanged("López"))
        env.viewModel.onPersonalEvent(RegisterFormEvent.DocumentChanged("12345678"))
        env.viewModel.onPersonalEvent(RegisterFormEvent.EmailChanged("ana@test.com"))
        env.viewModel.onPersonalEvent(RegisterFormEvent.PasswordChanged("secret12!"))
        env.viewModel.onPersonalEvent(RegisterFormEvent.ConfirmPasswordChanged("secret12!"))
        env.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertEquals(
            com.cocido.oficios.feature.auth.register.RegisterFormUiState.TERMS_REQUIRED_MARKER,
            env.viewModel.uiState.value.personal.termsError,
        )
        env.collectJob?.cancel()
    }

    @Test
    fun `back and rotation do not replay a consumed navigation event`() = runTest(dispatcher) {
        val env = createEnv()
        fillPersonal(env.viewModel)
        env.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        assertEquals(listOf(RegistrationWizardEffect.GoToProfile), env.effects)
        env.collectJob?.cancel()
        advanceUntilIdle()
        val afterRecreation = mutableListOf<RegistrationWizardEffect>()
        backgroundScope.launch(dispatcher) { env.viewModel.effects.collect { afterRecreation += it } }
        env.viewModel.onContactNameChanged("Edited after navigation")
        advanceUntilIdle()
        assertTrue(afterRecreation.isEmpty())
    }

    @Test
    fun `event buffered while stopped is delivered once on resume`() = runTest(dispatcher) {
        val env = createEnv()
        env.collectJob?.cancel()
        advanceUntilIdle()
        fillPersonal(env.viewModel)
        env.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        val resumed = mutableListOf<RegistrationWizardEffect>()
        val collector = launch(dispatcher) { env.viewModel.effects.collect { resumed += it } }
        advanceUntilIdle()
        assertEquals(listOf(RegistrationWizardEffect.GoToProfile), resumed)
        collector.cancel()
        advanceUntilIdle()
        val restarted = mutableListOf<RegistrationWizardEffect>()
        backgroundScope.launch(dispatcher) { env.viewModel.effects.collect { restarted += it } }
        advanceUntilIdle()
        assertTrue(restarted.isEmpty())
    }

    @Test
    fun `process recreation restores draft without issuing old navigation`() = runTest(dispatcher) {
        val old = createEnv()
        fillPersonal(old.viewModel)
        old.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        advanceUntilIdle()
        old.collectJob?.cancel()
        val recreated = createEnv(drafts = old.drafts)
        advanceUntilIdle()
        assertEquals("ana@test.com", recreated.viewModel.uiState.value.personal.email)
        assertEquals("", recreated.viewModel.uiState.value.personal.password)
        assertTrue(recreated.effects.isEmpty())
        recreated.collectJob?.cancel()
    }
    @Test
    fun `client review shows phone barrio and street from profile`() = runTest(dispatcher) {
        val env = createEnv()
        fillPersonal(env.viewModel)
        fillProfile(env.viewModel)
        advanceUntilIdle()
        val review = env.viewModel.uiState.value.review
        assertEquals("93705219109", review.phone)
        assertEquals("Centro", review.barrio)
        assertEquals("Belgrano 100", review.street)
        env.collectJob?.cancel()
    }

    @Test
    fun `provider review shows zona barrio after continueFromZona`() = runTest(dispatcher) {
        val env = createEnv()
        val viewModel = RegistrationWizardViewModel(
            savedStateHandle = SavedStateHandle(mapOf("role" to "PROVIDER")),
            draftRepository = env.drafts,
            committer = env.viewModel.let { _ ->
                RegistrationCommitter(
                    registerClientUseCase = RegisterClientUseCase(env.auth),
                    registerProviderUseCase = RegisterProviderUseCase(env.auth),
                    saveClientContactoUseCase = SaveClientContactoUseCase(FakeClientProfiles()),
                    saveClientDomicilioUseCase = SaveClientDomicilioUseCase(FakeClientProfiles()),
                    draftRepository = env.drafts,
                    registerTradeDraftRepository = object :
                        com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                        override suspend fun save(draft: RegisterTradeDraft) = Unit
                        override suspend fun get() = null
                        override suspend fun clear() = Unit
                    },
                    submitRegisterTradeDraftUseCase = noopTradeSubmit(),
                    sessionRepository = FakeSession(),
                )
            },
            searchBarriosUseCase = SearchBarriosUseCase(FakeCatalog()),
            passwordHolder = RegistrationPasswordHolder(),
        )
        fillPersonal(viewModel)
        viewModel.continueFromZona(
            RegisterTradeDraft(
                categoryId = 1,
                categoryName = "Hogar",
                rubroId = 2,
                rubroName = "Electricidad",
                subRubroIds = listOf(3),
                otherSpecialization = null,
                experienceRange = "3 a 5 años",
                neighborhoodQuery = "San Miguel",
                barrioId = 11,
                servesAtClientHome = false,
                servesAtLocal = true,
                timeSlots = emptyList(),
                localCalle = "Rivadavia",
                localNumero = "200",
            ),
        )
        advanceUntilIdle()
        val review = viewModel.uiState.value.review
        assertEquals(RegistrationRole.PROVIDER, viewModel.uiState.value.role)
        assertEquals("", review.phone)
        assertEquals("San Miguel", review.barrio)
        assertEquals(true, review.showStreet)
        assertEquals("Rivadavia 200", review.street)
        assertEquals("Hogar", review.oficioCategory)
        assertEquals("Electricidad", review.oficioRubro)
        assertEquals("En local propio", review.serviceArea)
        val saved = env.drafts.draft
        assertEquals("San Miguel", saved?.provider?.neighborhoodQuery)
        env.collectJob?.cancel()
    }

    @Test
    fun `edit section effects pop to the matching wizard step`() = runTest(dispatcher) {
        val env = createEnv()
        env.viewModel.editPersonal()
        env.viewModel.editProfile()
        env.viewModel.editOficio()
        env.viewModel.editZona()
        env.viewModel.editAgenda()
        env.viewModel.editCobro()
        env.viewModel.onReviewBack()
        advanceUntilIdle()
        assertEquals(
            listOf(
                RegistrationWizardEffect.GoToPersonal,
                RegistrationWizardEffect.PopToProfile,
                RegistrationWizardEffect.PopToOficio,
                RegistrationWizardEffect.PopToZona,
                RegistrationWizardEffect.PopToAgenda,
                RegistrationWizardEffect.PopToCobro,
                RegistrationWizardEffect.GoBack,
            ),
            env.effects,
        )
        env.collectJob?.cancel()
    }

    @Test
    fun `create account ignores a second tap while committing`() = runTest(dispatcher) {
        val env = createEnv()
        env.auth.registrationGate = kotlinx.coroutines.CompletableDeferred()
        fillPersonal(env.viewModel)
        env.viewModel.onPersonalEvent(RegisterFormEvent.Submit)
        fillProfile(env.viewModel)
        env.viewModel.continueFromProfile()
        env.viewModel.createAccount()
        runCurrent()
        assertEquals(true, env.viewModel.uiState.value.isCommitting)
        env.viewModel.createAccount()
        runCurrent()
        assertEquals(1, env.auth.clientAttempts)
        env.auth.registrationGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, env.auth.clientAttempts)
        env.collectJob?.cancel()
    }

    private fun fillPersonal(viewModel: RegistrationWizardViewModel) {
        viewModel.onPersonalEvent(RegisterFormEvent.FirstNameChanged("Ana"))
        viewModel.onPersonalEvent(RegisterFormEvent.LastNameChanged("López"))
        viewModel.onPersonalEvent(RegisterFormEvent.DocumentChanged("12345678"))
        viewModel.onPersonalEvent(RegisterFormEvent.EmailChanged("ana@test.com"))
        viewModel.onPersonalEvent(RegisterFormEvent.PasswordChanged("secret12!"))
        viewModel.onPersonalEvent(RegisterFormEvent.ConfirmPasswordChanged("secret12!"))
        viewModel.onPersonalEvent(RegisterFormEvent.TermsChanged(true))
    }

    private fun fillProfile(viewModel: RegistrationWizardViewModel) {
        viewModel.onContactNameChanged("Ana López")
        viewModel.onPhoneChanged("93705219109")
        viewModel.onBarrioSelected(CatalogOption(4, "Centro"))
        viewModel.onCalleChanged("Belgrano")
        viewModel.onNumeroChanged("100")
    }

    private fun createEnv(
        registerResult: AppResult<UserSession> = AppResult.Success(
            UserSession(
                tokens = AuthTokens("access", "refresh"),
                user = User(1, "ana@test.com", "Ana", "López", UserRole.CLIENT, false, personaId = 99),
            ),
        ),
        drafts: InMemoryDrafts = InMemoryDrafts(),
        passwordHolder: RegistrationPasswordHolder = RegistrationPasswordHolder(),
    ): Env {
        val auth = FakeAuth(registerResult)
        val profiles = FakeClientProfiles()
        val committer = RegistrationCommitter(
            registerClientUseCase = RegisterClientUseCase(auth),
            registerProviderUseCase = RegisterProviderUseCase(auth),
            saveClientContactoUseCase = SaveClientContactoUseCase(profiles),
            saveClientDomicilioUseCase = SaveClientDomicilioUseCase(profiles),
            draftRepository = drafts,
            registerTradeDraftRepository = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
                override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
                override suspend fun get() = null
                override suspend fun clear() = Unit
            },
            submitRegisterTradeDraftUseCase = noopTradeSubmit(),
            sessionRepository = FakeSession(),
        )
        val viewModel = RegistrationWizardViewModel(
            savedStateHandle = SavedStateHandle(mapOf("role" to "CLIENT")),
            draftRepository = drafts,
            committer = committer,
            searchBarriosUseCase = SearchBarriosUseCase(FakeCatalog()),
            passwordHolder = passwordHolder,
        )
        val effects = mutableListOf<RegistrationWizardEffect>()
        // Collect is started by tests via runTest + launch below through Env helper.
        return Env(viewModel, auth, drafts, effects).also { env ->
            env.collectJob = kotlinx.coroutines.CoroutineScope(dispatcher).launch {
                viewModel.effects.collect { env.effects += it }
            }
        }
    }

    private data class Env(
        val viewModel: RegistrationWizardViewModel,
        val auth: FakeAuth,
        val drafts: InMemoryDrafts,
        val effects: MutableList<RegistrationWizardEffect>,
        var collectJob: kotlinx.coroutines.Job? = null,
    )

    private class InMemoryDrafts(
        var draft: RegistrationDraft? = null,
    ) : RegistrationDraftRepository {
        val encodedHasPassword: Boolean
            get() = draft.toString().contains("password", ignoreCase = true)
        override suspend fun save(draft: RegistrationDraft) {
            this.draft = draft
        }
        override suspend fun get(): RegistrationDraft? = draft
        override suspend fun clear() {
            draft = null
        }
    }

    private class FakeAuth(
        var registerClientResult: AppResult<UserSession>,
    ) : AuthRepository {
        var clientAttempts = 0
        var registrationGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null
        override suspend fun login(email: String, password: String) = AppResult.Error(AppError.Unknown())
        override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> {
            clientAttempts++
            registrationGate?.await()
            return registerClientResult
        }
        override suspend fun registerProvider(request: RegisterProviderRequest) = AppResult.Error(AppError.Unknown())
        override suspend fun refreshToken(refreshToken: String) = AppResult.Error(AppError.Unknown())
        override suspend fun getCurrentUser() = AppResult.Error(AppError.Unknown())
        override suspend fun logout() = AppResult.Success(Unit)
        override suspend fun requestPasswordReset(email: String) = AppResult.Success(Unit)
        override suspend fun verifyPasswordResetCode(code: String) = AppResult.Error(AppError.Unknown())
        override suspend fun resetPassword(resetToken: String, password: String, confirmPassword: String) =
            AppResult.Error(AppError.Unknown())
        override suspend fun sendVerificationEmail() = AppResult.Success("ok")
        override suspend fun verifyEmail(code: String) = AppResult.Success(Unit)
        override suspend fun getUserRoles() = AppResult.Success(emptyList<com.cocido.oficios.domain.model.UserRoleDefinition>())
    }

    private class FakeSession : SessionRepository {
        override val authState: Flow<AuthState> = flowOf(AuthState.Unauthenticated)
        override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
        override suspend fun clearSession() = Unit
        override suspend fun getAccessToken(): String? = null
        override suspend fun getRefreshToken(): String? = null
        override suspend fun updateTokens(tokens: AuthTokens) = Unit
        override suspend fun getAccessTokenExpiresAtMillis(): Long? = null
        override suspend fun getCachedUser(): User? = null
        override suspend fun updateCachedUser(user: User) = Unit
    }

    private class FakeClientProfiles : ClientProfileRepository {
        override suspend fun saveDomicilio(request: ClientDomicilioRequest) = AppResult.Success(Unit)
        override suspend fun saveContacto(request: ClientContactoRequest) = AppResult.Success(1L)
        override suspend fun updateDomicilio(id: Long, request: ClientDomicilioRequest) = AppResult.Success(Unit)
        override suspend fun updateContacto(id: Long, request: ClientContactoRequest) = AppResult.Success(Unit)
        override suspend fun sendPhoneVerificationCode(contactId: Long) =
            AppResult.Success(PhoneVerificationSendResult("ok", null))
        override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
        override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> = AppResult.Success(emptyList())
        override suspend fun getMyContactos(): AppResult<List<ClientContacto>> = AppResult.Success(emptyList())
    }

    private class FakeCatalog : CatalogRepository {
        override suspend fun getCategories() = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getRubrosByCategory(categoryId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun getSubRubrosByRubro(rubroId: Long) = AppResult.Success(emptyList<CatalogOption>())
        override suspend fun searchBarrios(query: String) = AppResult.Success(listOf(CatalogOption(4, "Centro")))
    }

    private fun noopTradeSubmit(): com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase {
        val tradeDrafts = object : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
            override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) = Unit
            override suspend fun get() = null
            override suspend fun clear() = Unit
        }
        val provider = object : com.cocido.oficios.domain.repository.ProviderProfileRepository {
            override suspend fun updateProfessionalInfo(
                request: com.cocido.oficios.domain.model.ProviderProfessionalInfoRequest,
            ) = AppResult.Success(Unit)
            override suspend fun submitRubroFull(
                request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
            ) = AppResult.Success(Unit)
            override suspend fun submitSecondaryRubroFull(
                request: com.cocido.oficios.domain.model.ProviderRubroFullRequest,
            ) = AppResult.Success(Unit)
            override suspend fun finalizeProfile() = AppResult.Success(Unit)
            override suspend fun loadProviderProfileForSync() = AppResult.Success(
                com.cocido.oficios.domain.model.ProviderProfileSync(null, emptyList()),
            )
            override suspend fun getMySubRubros() =
                AppResult.Success(emptyList<com.cocido.oficios.domain.model.ProviderSubRubroOption>())
        }
        val catalog = FakeCatalog()
        return com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase(
            providerProfileRepository = provider,
            registerTradeDraftRepository = tradeDrafts,
            catalogRepository = catalog,
            syncRegisterTradeDraftFromProfileUseCase =
                com.cocido.oficios.domain.usecase.profile.SyncRegisterTradeDraftFromProfileUseCase(
                    provider,
                    tradeDrafts,
                ),
            syncProviderBarrioDomicilio = object :
                com.cocido.oficios.domain.usecase.profile.ProviderBarrioDomicilioSync {
                override suspend fun invoke(barrioId: Long, barrioName: String) = AppResult.Success(Unit)
            },
        )
    }
}
