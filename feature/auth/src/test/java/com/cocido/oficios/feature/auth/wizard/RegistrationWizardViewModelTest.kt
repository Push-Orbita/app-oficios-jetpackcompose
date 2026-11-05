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
import com.cocido.oficios.domain.model.RegistrationDraft
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
    fun `restores draft fields but never a password`() = runTest(dispatcher) {
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

    private fun fillPersonal(viewModel: RegistrationWizardViewModel) {
        viewModel.onPersonalEvent(RegisterFormEvent.FirstNameChanged("Ana"))
        viewModel.onPersonalEvent(RegisterFormEvent.LastNameChanged("López"))
        viewModel.onPersonalEvent(RegisterFormEvent.DocumentChanged("12345678"))
        viewModel.onPersonalEvent(RegisterFormEvent.EmailChanged("ana@test.com"))
        viewModel.onPersonalEvent(RegisterFormEvent.PasswordChanged("secret123"))
        viewModel.onPersonalEvent(RegisterFormEvent.ConfirmPasswordChanged("secret123"))
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
    ): Env {
        val auth = FakeAuth(registerResult)
        val profiles = FakeClientProfiles()
        val committer = RegistrationCommitter(
            registerClientUseCase = RegisterClientUseCase(auth),
            registerProviderUseCase = RegisterProviderUseCase(auth),
            saveClientContactoUseCase = SaveClientContactoUseCase(profiles),
            saveClientDomicilioUseCase = SaveClientDomicilioUseCase(profiles),
            draftRepository = drafts,
            sessionRepository = FakeSession(),
        )
        val viewModel = RegistrationWizardViewModel(
            savedStateHandle = SavedStateHandle(mapOf("role" to "CLIENT")),
            draftRepository = drafts,
            committer = committer,
            searchBarriosUseCase = SearchBarriosUseCase(FakeCatalog()),
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
        override suspend fun login(email: String, password: String) = AppResult.Error(AppError.Unknown())
        override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> {
            clientAttempts++
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
}
