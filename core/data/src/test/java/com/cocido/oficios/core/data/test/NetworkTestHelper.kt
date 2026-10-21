package com.cocido.oficios.core.data.test

import com.cocido.oficios.core.network.api.auth.AuthApi
import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.ProviderProfileApi
import com.cocido.oficios.core.network.UnitResponseConverterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit

internal object NetworkTestHelper {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    fun createProviderProfileApi(server: MockWebServer): ProviderProfileApi =
        createRetrofit(server).create(ProviderProfileApi::class.java)

    fun createAuthApi(server: MockWebServer): AuthApi =
        createRetrofit(server).create(AuthApi::class.java)

    fun createClientProfileApi(server: MockWebServer): ClientProfileApi =
        createRetrofit(server).create(ClientProfileApi::class.java)

    fun loadFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Fixture not found: $path"
        }.bufferedReader().use { it.readText() }

    private fun createRetrofit(server: MockWebServer): Retrofit =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(UnitResponseConverterFactory)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
