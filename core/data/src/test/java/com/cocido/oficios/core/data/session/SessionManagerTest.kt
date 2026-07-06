package com.cocido.oficios.core.data.session

import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SessionManagerTest {

    private lateinit var tokenDataStore: TokenDataStore
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        tokenDataStore = TokenDataStore(context)
        tokenDataStore.clear()
        sessionManager = SessionManager(tokenDataStore)
    }

    @Test
    fun `save session emits authenticated state`() = runTest {
        val user = User(1, "user@test.com", "Juan", "Pérez", UserRole.CLIENT, true)
        val tokens = AuthTokens("access", "refresh")
        sessionManager.saveSession(tokens, user)

        val state = sessionManager.authState.first()
        assertTrue(state is AuthState.Authenticated)
        assertEquals("access", sessionManager.getAccessToken())
    }

    @Test
    fun `clear session resets state`() = runTest {
        val user = User(1, "user@test.com", "Juan", "Pérez", UserRole.CLIENT, true)
        sessionManager.saveSession(AuthTokens("access", "refresh"), user)
        sessionManager.clearSession()

        val state = sessionManager.authState.first()
        assertTrue(state is AuthState.Unauthenticated)
        assertNull(sessionManager.getAccessToken())
    }
}
