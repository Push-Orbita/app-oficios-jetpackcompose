package com.cocido.oficios.core.network.authenticator

import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.auth.dto.RefreshTokenRequestDto
import com.cocido.oficios.core.network.PublicEndpoints
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val authApiProvider: Provider<AuthApi>,
) : Authenticator {

    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRY) return null

        val path = response.request.url.encodedPath.trimStart('/')
        if (PublicEndpoints.isPublic(path)) return null

        val refreshToken = tokenDataStore.getRefreshToken() ?: run {
            tokenDataStore.clear()
            return null
        }

        return runBlocking {
            refreshMutex.withLock {
                val currentToken = tokenDataStore.getAccessToken()
                val requestToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                    ?.trim()

                if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshResult = runCatching {
                    authApiProvider.get().refreshToken(RefreshTokenRequestDto(refreshToken))
                }.getOrNull()

                if (refreshResult == null) {
                    tokenDataStore.clear()
                    return@withLock null
                }

                tokenDataStore.updateTokens(
                    refreshResult.accessToken,
                    refreshResult.refreshToken,
                )

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshResult.accessToken}")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val MAX_RETRY = 2
    }
}
