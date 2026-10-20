package com.cocido.oficios.core.network.test

import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.IdentityDocumentApi
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.api.search.WorkerSearchApi
import com.cocido.oficios.core.network.api.worker.WorkerProfileApi
import com.cocido.oficios.core.network.api.worker.WorkerReviewsApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
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

    fun createRetrofit(server: MockWebServer): Retrofit =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    fun createProviderProfileApi(server: MockWebServer): ProviderProfileApi =
        createRetrofit(server).create(ProviderProfileApi::class.java)

    fun createClientProfileApi(server: MockWebServer): ClientProfileApi =
        createRetrofit(server).create(ClientProfileApi::class.java)

    fun createIdentityDocumentApi(server: MockWebServer): IdentityDocumentApi =
        createRetrofit(server).create(IdentityDocumentApi::class.java)

    fun createWorkerSearchApi(server: MockWebServer): WorkerSearchApi =
        createRetrofit(server).create(WorkerSearchApi::class.java)

    fun createWorkerProfileApi(server: MockWebServer): WorkerProfileApi =
        createRetrofit(server).create(WorkerProfileApi::class.java)

    fun createWorkerReviewsApi(server: MockWebServer): WorkerReviewsApi =
        createRetrofit(server).create(WorkerReviewsApi::class.java)

    fun loadFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Fixture not found: $path"
        }.bufferedReader().use { it.readText() }
}
