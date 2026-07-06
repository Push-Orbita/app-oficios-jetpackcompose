package com.cocido.oficios.core.network.session

import com.cocido.oficios.core.common.auth.TokenExpiry
import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenRequestDto
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class TokenRefreshCoordinator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val authApiProvider: Provider<AuthApi>,
) {
    private val refreshMutex = Mutex()

    fun refreshIfNeededBlocking(): Boolean = runBlocking {
        refreshIfNeeded()
    }

    suspend fun refreshIfNeeded(): Boolean {
        val expiresAt = tokenDataStore.getAccessTokenExpiresAtMillis()
        if (!TokenExpiry.shouldRefresh(expiresAt)) return false
        return refreshBlockingInternal()
    }

    suspend fun refreshBlockingInternal(force: Boolean = false): Boolean = refreshMutex.withLock {
        val expiresAt = tokenDataStore.getAccessTokenExpiresAtMillis()
        if (!force && !TokenExpiry.shouldRefresh(expiresAt)) return true

        val refreshToken = tokenDataStore.getRefreshToken() ?: run {
            tokenDataStore.clear()
            return false
        }

        val refreshResult = runCatching {
            authApiProvider.get().refreshToken(RefreshTokenRequestDto(refreshToken))
        }.getOrNull()

        if (refreshResult == null) {
            tokenDataStore.clear()
            return false
        }

        tokenDataStore.updateTokensFromExpiresIn(
            accessToken = refreshResult.accessToken,
            refreshToken = refreshResult.refreshToken,
            expiresIn = refreshResult.expiresIn,
        )
        true
    }
}
