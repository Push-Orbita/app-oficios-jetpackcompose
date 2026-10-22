package com.cocido.oficios.core.network.test

import com.cocido.oficios.core.network.api.account.WorkGalleryApi
import com.cocido.oficios.core.network.api.agenda.AgendaApi
import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.catalog.CatalogApi
import com.cocido.oficios.core.network.api.notifications.NotificationsApi
import com.cocido.oficios.core.network.api.orders.OrdersApi
import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.IdentityDocumentApi
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.api.search.WorkerSearchApi
import com.cocido.oficios.core.network.api.social.FavoritesApi
import com.cocido.oficios.core.network.api.worker.WorkerProfileApi
import com.cocido.oficios.core.network.api.worker.WorkerReviewsApi
import com.cocido.oficios.core.network.JsonElementLenientConverterFactory
import com.cocido.oficios.core.network.UnitResponseConverterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit

object NetworkTestFactory {

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    fun createRetrofit(server: MockWebServer, timeoutMs: Long = 10_000): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(UnitResponseConverterFactory)
            .addConverterFactory(JsonElementLenientConverterFactory)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    fun createProviderProfileApi(server: MockWebServer, timeoutMs: Long = 10_000): ProviderProfileApi =
        createRetrofit(server, timeoutMs).create(ProviderProfileApi::class.java)

    fun createClientProfileApi(server: MockWebServer, timeoutMs: Long = 10_000): ClientProfileApi =
        createRetrofit(server, timeoutMs).create(ClientProfileApi::class.java)

    fun createIdentityDocumentApi(server: MockWebServer, timeoutMs: Long = 10_000): IdentityDocumentApi =
        createRetrofit(server, timeoutMs).create(IdentityDocumentApi::class.java)

    fun createWorkerSearchApi(server: MockWebServer, timeoutMs: Long = 10_000): WorkerSearchApi =
        createRetrofit(server, timeoutMs).create(WorkerSearchApi::class.java)

    fun createWorkerProfileApi(server: MockWebServer, timeoutMs: Long = 10_000): WorkerProfileApi =
        createRetrofit(server, timeoutMs).create(WorkerProfileApi::class.java)

    fun createWorkerReviewsApi(server: MockWebServer, timeoutMs: Long = 10_000): WorkerReviewsApi =
        createRetrofit(server, timeoutMs).create(WorkerReviewsApi::class.java)

    fun createAuthApi(server: MockWebServer, timeoutMs: Long = 10_000): AuthApi =
        createRetrofit(server, timeoutMs).create(AuthApi::class.java)

    fun createOrdersApi(server: MockWebServer, timeoutMs: Long = 10_000): OrdersApi =
        createRetrofit(server, timeoutMs).create(OrdersApi::class.java)

    fun createNotificationsApi(server: MockWebServer, timeoutMs: Long = 10_000): NotificationsApi =
        createRetrofit(server, timeoutMs).create(NotificationsApi::class.java)

    fun createFavoritesApi(server: MockWebServer, timeoutMs: Long = 10_000): FavoritesApi =
        createRetrofit(server, timeoutMs).create(FavoritesApi::class.java)

    fun createCatalogApi(server: MockWebServer, timeoutMs: Long = 10_000): CatalogApi =
        createRetrofit(server, timeoutMs).create(CatalogApi::class.java)

    fun createAgendaApi(server: MockWebServer, timeoutMs: Long = 10_000): AgendaApi =
        createRetrofit(server, timeoutMs).create(AgendaApi::class.java)

    fun createWorkGalleryApi(server: MockWebServer, timeoutMs: Long = 10_000): WorkGalleryApi =
        createRetrofit(server, timeoutMs).create(WorkGalleryApi::class.java)

    fun loadFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Fixture not found: $path"
        }.bufferedReader().use { it.readText() }
}
