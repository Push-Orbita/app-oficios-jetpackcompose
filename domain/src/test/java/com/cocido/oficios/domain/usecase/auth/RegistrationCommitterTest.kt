package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.domain.model.ClientDomicilioRequest
import com.cocido.oficios.domain.model.PhoneVerificationSendResult
import com.cocido.oficios.domain.model.RegisterClientRequest
import com.cocido.oficios.domain.model.RegisterProviderRequest
import com.cocido.oficios.domain.model.RegistrationClientDraft
import com.cocido.oficios.domain.model.RegistrationCommitPhase
import com.cocido.oficios.domain.model.RegistrationDraft
import com.cocido.oficios.domain.model.RegistrationPersonalDraft
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.AuthRepository
import com.cocido.oficios.domain.repository.ClientProfileRepository
import com.cocido.oficios.domain.repository.RegistrationDraftRepository
import com.cocido.oficios.domain.repository.SessionRepository
import com.cocido.oficios.domain.usecase.profile.SaveClientContactoUseCase
import com.cocido.oficios.domain.usecase.profile.SaveClientDomicilioUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationCommitterTest {

    @Test
    fun `409 on signup returns EmailConflict and keeps draft`() = runTest {
        val incoming = sampleDraft()
        val drafts = InMemoryDraftRepository(incoming)
        val auth = FakeAuth(
            registerClientResult = AppResult.Error(AppError.Conflict("Este correo ya está registrado")),
        )
        val committer = committer(auth, drafts)

        val result = committer.commit(incoming, "secret123")

        assertTrue(result is RegistrationCommitResult.EmailConflict)
        assertEquals("Este correo ya está registrado", (result as RegistrationCommitResult.EmailConflict).message)
        assertEquals("ana@test.com", drafts.draft?.personal?.email)
        assertEquals(1, auth.clientAttempts)
    }

    @Test
    fun `skips signup when session matches draft email`() = runTest {
        val drafts = InMemoryDraftRepository(
            RegistrationDraft(
                personal = samplePersonal(),
                client = sampleClient(),
                commitPhase = RegistrationCommitPhase.ACCOUNT_CREATED,
                createdUserEmail = "ana@test.com",
            ),
        )
        val session = FakeSession(
            token = "access",
            user = sampleUser(),
        )
        val auth = FakeAuth()
        val profiles = FakeClientProfiles()
        val committer = committer(auth, drafts, session, profiles)

        val result = committer.commit(sampleDraft(), "secret123")

        assertEquals(RegistrationCommitResult.Completed, result)
        assertEquals(0, auth.clientAttempts)
        assertEquals(1, profiles.createdContactos)
        assertEquals(1, profiles.createdDomicilios)
        assertNull(drafts.draft)
    }

    @Test
    fun `client commit signs up then saves contacto and domicilio`() = runTest {
        val drafts = InMemoryDraftRepository()
        val auth = FakeAuth()
        val profiles = FakeClientProfiles()
        val committer = committer(auth, drafts, FakeSession(), profiles)

        val result = committer.commit(sampleDraft(), "secret123")

        assertEquals(RegistrationCommitResult.Completed, result)
        assertEquals(1, auth.clientAttempts)
        assertEquals("secret123", auth.lastClientRequest?.password)
        assertEquals(1, profiles.createdContactos)
        assertEquals(1, profiles.createdDomicilios)
        assertNull(drafts.draft)
    }

    @Test
    fun `resume after ACCOUNT_CREATED does not sign up again`() = runTest {
        val drafts = InMemoryDraftRepository(
            sampleDraft().copy(commitPhase = RegistrationCommitPhase.ACCOUNT_CREATED),
        )
        val auth = FakeAuth()
        val session = FakeSession(token = "access", user = sampleUser())
        val profiles = FakeClientProfiles()
        val committer = committer(auth, drafts, session, profiles)

        val incoming = sampleDraft().copy(commitPhase = RegistrationCommitPhase.NOT_STARTED)
        val result = committer.commit(incoming, "secret123")

        assertEquals(RegistrationCommitResult.Completed, result)
        assertEquals(0, auth.clientAttempts)
        assertEquals(1, profiles.createdDomicilios)
    }

    @Test
    fun `provider commit signs up and keeps trade draft for onboarding retry`() = runTest {
        val trade = com.cocido.oficios.domain.model.RegisterTradeDraft(
            categoryId = 1,
            categoryName = "Hogar",
            rubroId = 2,
            rubroName = "Plomería",
            subRubroIds = listOf(3),
            otherSpecialization = null,
            experienceRange = "3 a 5 años",
            neighborhoodQuery = "Centro",
            barrioId = 4,
            timeSlots = listOf(com.cocido.oficios.domain.model.RegisterTradeTimeSlotDraft("09:00", "18:00")),
            paymentMethods = listOf("EFECTIVO"),
        )
        val drafts = InMemoryDraftRepository()
        val tradeDrafts = FakeTradeDrafts()
        val auth = FakeAuth(
            registerClientResult = AppResult.Error(AppError.Unknown()),
        ).apply {
            registerProviderResult = AppResult.Success(
                UserSession(
                    tokens = AuthTokens("access", "refresh"),
                    user = User(1, "ana@test.com", "Ana", "López", UserRole.PROVIDER, false, personaId = 99),
                ),
            )
        }
        val committer = RegistrationCommitter(
            registerClientUseCase = RegisterClientUseCase(auth),
            registerProviderUseCase = RegisterProviderUseCase(auth),
            saveClientContactoUseCase = SaveClientContactoUseCase(FakeClientProfiles()),
            saveClientDomicilioUseCase = SaveClientDomicilioUseCase(FakeClientProfiles()),
            draftRepository = drafts,
            registerTradeDraftRepository = tradeDrafts,
            submitRegisterTradeDraftUseCase = noopTradeSubmit(tradeDrafts),
            sessionRepository = FakeSession(),
        )

        val result = committer.commit(
            sampleDraft().copy(role = RegistrationRole.PROVIDER, provider = trade),
            "secret123",
        )

        assertEquals(RegistrationCommitResult.Completed, result)
        assertEquals(1, auth.providerAttempts)
        assertEquals(2L, tradeDrafts.draft?.rubroId)
        assertNull(drafts.draft)
    }

    private fun committer(
        auth: FakeAuth,
        drafts: InMemoryDraftRepository,
        session: FakeSession = FakeSession(),
        profiles: FakeClientProfiles = FakeClientProfiles(),
    ) = RegistrationCommitter(
        registerClientUseCase = RegisterClientUseCase(auth),
        registerProviderUseCase = RegisterProviderUseCase(auth),
        saveClientContactoUseCase = SaveClientContactoUseCase(profiles),
        saveClientDomicilioUseCase = SaveClientDomicilioUseCase(profiles),
        draftRepository = drafts,
        registerTradeDraftRepository = FakeTradeDrafts(),
        submitRegisterTradeDraftUseCase = noopTradeSubmit(),
        sessionRepository = session,
    )

    private fun sampleDraft() = RegistrationDraft(
        role = RegistrationRole.CLIENT,
        personal = samplePersonal(),
        client = sampleClient(),
    )

    private fun samplePersonal() = RegistrationPersonalDraft(
        firstName = "Ana",
        lastName = "López",
        email = "ana@test.com",
        document = "12345678",
        acceptedTerms = true,
    )

    private fun sampleClient() = RegistrationClientDraft(
        contactName = "Ana López",
        phone = "93705219109",
        barrioQuery = "Centro",
        barrioId = 4,
        calle = "Belgrano",
        numero = "100",
    )

    private fun sampleUser() = User(
        id = 1,
        email = "ana@test.com",
        firstName = "Ana",
        lastName = "López",
        role = UserRole.CLIENT,
        emailVerified = false,
        personaId = 99,
    )

    private class InMemoryDraftRepository(
        var draft: RegistrationDraft? = null,
    ) : RegistrationDraftRepository {
        override suspend fun save(draft: RegistrationDraft) {
            this.draft = draft
        }
        override suspend fun get(): RegistrationDraft? = draft
        override suspend fun clear() {
            draft = null
        }
    }

    private class FakeSession(
        private val token: String? = null,
        private val user: User? = null,
    ) : SessionRepository {
        override val authState: Flow<AuthState> = flowOf(AuthState.Unauthenticated)
        override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
        override suspend fun clearSession() = Unit
        override suspend fun getAccessToken(): String? = token
        override suspend fun getRefreshToken(): String? = null
        override suspend fun updateTokens(tokens: AuthTokens) = Unit
        override suspend fun getAccessTokenExpiresAtMillis(): Long? = null
        override suspend fun getCachedUser(): User? = user
        override suspend fun updateCachedUser(user: User) = Unit
    }

    private class FakeAuth(
        var registerClientResult: AppResult<UserSession> = AppResult.Success(
            UserSession(
                tokens = AuthTokens("access", "refresh"),
                user = User(1, "ana@test.com", "Ana", "López", UserRole.CLIENT, false, personaId = 99),
            ),
        ),
        var registerProviderResult: AppResult<UserSession> = AppResult.Error(AppError.Unknown()),
    ) : AuthRepository {
        var clientAttempts = 0
        var providerAttempts = 0
        var lastClientRequest: RegisterClientRequest? = null
        override suspend fun login(email: String, password: String) = AppResult.Error(AppError.Unknown())
        override suspend fun registerClient(request: RegisterClientRequest): AppResult<UserSession> {
            clientAttempts++
            lastClientRequest = request
            return registerClientResult
        }
        override suspend fun registerProvider(request: RegisterProviderRequest): AppResult<UserSession> {
            providerAttempts++
            return registerProviderResult
        }
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

    private class FakeClientProfiles : ClientProfileRepository {
        var createdContactos = 0
        var createdDomicilios = 0
        override suspend fun saveDomicilio(request: ClientDomicilioRequest): AppResult<Unit> {
            createdDomicilios++
            return AppResult.Success(Unit)
        }
        override suspend fun saveContacto(request: ClientContactoRequest): AppResult<Long> {
            createdContactos++
            return AppResult.Success(1L)
        }
        override suspend fun updateDomicilio(id: Long, request: ClientDomicilioRequest) = AppResult.Success(Unit)
        override suspend fun updateContacto(id: Long, request: ClientContactoRequest) = AppResult.Success(Unit)
        override suspend fun sendPhoneVerificationCode(contactId: Long) =
            AppResult.Success(PhoneVerificationSendResult("ok", null))
        override suspend fun verifyPhoneCode(contactId: Long, code: String) = AppResult.Success(Unit)
        override suspend fun getMyDomicilios(): AppResult<List<ClientDomicilio>> = AppResult.Success(emptyList())
        override suspend fun getMyContactos(): AppResult<List<ClientContacto>> = AppResult.Success(emptyList())
    }

    private class FakeTradeDrafts(
        var draft: com.cocido.oficios.domain.model.RegisterTradeDraft? = null,
    ) : com.cocido.oficios.domain.repository.RegisterTradeDraftRepository {
        override suspend fun save(draft: com.cocido.oficios.domain.model.RegisterTradeDraft) {
            this.draft = draft
        }
        override suspend fun get() = draft
        override suspend fun clear() {
            draft = null
        }
    }

    private fun noopTradeSubmit(
        tradeDrafts: FakeTradeDrafts = FakeTradeDrafts(),
    ): com.cocido.oficios.domain.usecase.profile.SubmitRegisterTradeDraftUseCase {
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
        val catalog = object : com.cocido.oficios.domain.repository.CatalogRepository {
            override suspend fun getCategories() =
                AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
            override suspend fun getRubrosByCategory(categoryId: Long) =
                AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
            override suspend fun getSubRubrosByRubro(rubroId: Long) =
                AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
            override suspend fun searchBarrios(query: String) =
                AppResult.Success(emptyList<com.cocido.oficios.domain.model.CatalogOption>())
        }
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
