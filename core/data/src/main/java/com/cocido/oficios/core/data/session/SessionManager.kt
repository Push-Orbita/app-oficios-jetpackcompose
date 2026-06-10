package com.cocido.oficios.core.data.session

import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.UserSession
import com.cocido.oficios.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class SessionManager @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : SessionRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: Flow<AuthState> = _authState.asStateFlow()

    init {
        restoreFromStorage()
    }

    override suspend fun saveSession(tokens: AuthTokens, user: User) {
        tokenDataStore.saveSession(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            userId = user.id,
            userRole = user.role.name,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            emailVerified = user.emailVerified,
        )
        _authState.update { AuthState.Authenticated(UserSession(tokens = tokens, user = user)) }
    }

    override suspend fun clearSession() {
        tokenDataStore.clear()
        _authState.update { AuthState.Unauthenticated }
    }

    override suspend fun getAccessToken(): String? = tokenDataStore.getAccessToken()

    override suspend fun getRefreshToken(): String? = tokenDataStore.getRefreshToken()

    override suspend fun updateTokens(tokens: AuthTokens) {
        tokenDataStore.updateTokens(tokens.accessToken, tokens.refreshToken)
        val current = _authState.value
        if (current is AuthState.Authenticated) {
            _authState.update {
                AuthState.Authenticated(current.session.copy(tokens = tokens))
            }
        }
    }

    override suspend fun getCachedUser(): User? {
        val id = tokenDataStore.getUserId() ?: return null
        val email = tokenDataStore.getUserEmail() ?: return null
        return User(
            id = id,
            email = email,
            firstName = tokenDataStore.getUserFirstName().orEmpty(),
            lastName = tokenDataStore.getUserLastName().orEmpty(),
            role = UserRole.valueOf(tokenDataStore.getUserRole() ?: UserRole.UNKNOWN.name),
            emailVerified = tokenDataStore.isEmailVerified(),
        )
    }

    fun hasStoredSession(): Boolean = tokenDataStore.hasSession()

    private fun restoreFromStorage() {
        if (!tokenDataStore.hasSession()) return
        val user = runCatching {
            val id = tokenDataStore.getUserId() ?: return
            val email = tokenDataStore.getUserEmail() ?: return
            val access = tokenDataStore.getAccessToken() ?: return
            val refresh = tokenDataStore.getRefreshToken() ?: return
            val cachedUser = User(
                id = id,
                email = email,
                firstName = tokenDataStore.getUserFirstName().orEmpty(),
                lastName = tokenDataStore.getUserLastName().orEmpty(),
                role = runCatching {
                    UserRole.valueOf(tokenDataStore.getUserRole() ?: UserRole.UNKNOWN.name)
                }.getOrDefault(UserRole.UNKNOWN),
                emailVerified = tokenDataStore.isEmailVerified(),
            )
            UserSession(tokens = AuthTokens(access, refresh), user = cachedUser)
        }.getOrNull() ?: return

        _authState.update { AuthState.Authenticated(user) }
    }
}
