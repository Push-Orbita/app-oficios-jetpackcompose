package com.cocido.oficios.core.network.authenticator

import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.core.network.PublicEndpoints
import com.cocido.oficios.core.network.session.TokenRefreshCoordinator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRY) return null

        val path = response.request.url.encodedPath.trimStart('/')
        if (PublicEndpoints.isPublic(path)) return null

        val requestToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
        val currentToken = tokenDataStore.getAccessToken()

        if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        val refreshed = runBlocking {
            tokenRefreshCoordinator.refreshBlockingInternal(force = true)
        }
        if (!refreshed) return null

        val newToken = tokenDataStore.getAccessToken() ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
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
