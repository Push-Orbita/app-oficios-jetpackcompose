package com.cocido.oficios.core.network.interceptor

import com.cocido.oficios.core.datastore.TokenDataStore
import com.cocido.oficios.core.network.PublicEndpoints
import com.cocido.oficios.core.network.session.TokenRefreshCoordinator
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        if (PublicEndpoints.isPublic(path)) {
            return chain.proceed(request)
        }

        tokenRefreshCoordinator.refreshIfNeededBlocking()

        val token = tokenDataStore.getAccessToken()
        val authenticatedRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}
