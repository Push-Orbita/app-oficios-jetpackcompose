package com.cocido.oficios.domain.repository

import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.AuthTokens
import com.cocido.oficios.domain.model.User
import com.cocido.oficios.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val authState: Flow<AuthState>
    suspend fun saveSession(tokens: AuthTokens, user: User)
    suspend fun clearSession()
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun updateTokens(tokens: AuthTokens)
    suspend fun getCachedUser(): User?
    suspend fun updateCachedUser(user: User)
}
