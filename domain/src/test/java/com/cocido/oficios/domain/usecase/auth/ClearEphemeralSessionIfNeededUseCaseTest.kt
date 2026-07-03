package com.cocido.oficios.domain.usecase.auth

import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.SessionPreferencesRepository
import com.cocido.oficios.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearEphemeralSessionIfNeededUseCaseTest {

    @Test
    fun `clears session when ephemeral`() = runTest {
        var cleared = false
        val useCase = ClearEphemeralSessionIfNeededUseCase(
            sessionPreferencesRepository = object : SessionPreferencesRepository {
                override fun setSessionEphemeral(ephemeral: Boolean) = Unit
                override fun isSessionEphemeral(): Boolean = true
            },
            sessionRepository = FakeSessionRepository(onClear = { cleared = true }),
        )

        useCase()

        assertTrue(cleared)
    }

    @Test
    fun `does not clear session when not ephemeral`() = runTest {
        var cleared = false
        val useCase = ClearEphemeralSessionIfNeededUseCase(
            sessionPreferencesRepository = object : SessionPreferencesRepository {
                override fun setSessionEphemeral(ephemeral: Boolean) = Unit
                override fun isSessionEphemeral(): Boolean = false
            },
            sessionRepository = FakeSessionRepository(onClear = { cleared = true }),
        )

        useCase()

        assertFalse(cleared)
    }

    private class FakeSessionRepository(
        private val onClear: () -> Unit,
    ) : SessionRepository {
        override val authState: Flow<AuthState> = flowOf(AuthState.Unauthenticated)
        override suspend fun saveSession(tokens: AuthTokens, user: User) = Unit
        override suspend fun clearSession() = onClear()
        override suspend fun getAccessToken(): String? = null
        override suspend fun getRefreshToken(): String? = null
        override suspend fun updateTokens(tokens: AuthTokens) = Unit
        override suspend fun getAccessTokenExpiresAtMillis(): Long? = null
        override suspend fun getCachedUser(): User? = null
        override suspend fun updateCachedUser(user: User) = Unit
    }
}
