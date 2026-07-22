package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.data.remote.CatalogRemoteDataSource
import com.cocido.oficios.core.network.api.catalog.CatalogApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class CatalogRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: CatalogRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CatalogApi::class.java)

        repository = CatalogRepositoryImpl(
            catalogRemoteDataSource = CatalogRemoteDataSource(api),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `searchBarrios calls backend without session`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "data": [
                        { "id": 11, "nombre": "Miguel Ortiz" },
                        { "id": 12, "nombre": "San Miguel" }
                      ],
                      "metadata": {
                        "count": 2,
                        "pageSize": 20,
                        "pageNumber": 1,
                        "totalPages": 1
                      }
                    }
                    """.trimIndent(),
                ),
        )

        val result = repository.searchBarrios("miguel")

        assertTrue(result is AppResult.Success)
        result as AppResult.Success
        assertEquals(listOf("Miguel Ortiz", "San Miguel"), result.data.map { it.label })

        val request = server.takeRequest()
        assertEquals("/barrios/search?q=miguel&pageSize=20&pageNumber=1", request.path)
    }
}
