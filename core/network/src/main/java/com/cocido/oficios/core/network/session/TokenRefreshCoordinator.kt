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
import retrofit2.HttpException

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

        val refreshToken = tokenDataStore.getRefreshToken() ?: return false

        val refreshResult = runCatching {
            authApiProvider.get().refreshToken(RefreshTokenRequestDto(refreshToken))
        }

        refreshResult.fold(
            onSuccess = { body ->
                tokenDataStore.updateTokensFromExpiresIn(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresIn = body.expiresIn,
                )
                true
            },
            onFailure = { error ->
                // UnknownHost, timeout y 5xx no son una sesión inválida.
                if (shouldClearSessionAfterRefresh(error)) {
                    tokenDataStore.clear()
                }
                false
            },
        )
    }
}

/** Solo un 401/403 del refresh cierra la sesión. Un corte de red no. */
internal fun shouldClearSessionAfterRefresh(error: Throwable): Boolean {
    val http = generateSequence(error) { it.cause }
        .filterIsInstance<HttpException>()
        .firstOrNull()
        ?: return false
    return http.code() == 401 || http.code() == 403
}
