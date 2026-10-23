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
        assertEquals("/barrios/search?q=miguel&nombre=miguel&pageSize=20&pageNumber=1", request.path)
    }

    @Test
    fun `searchBarrios Centro matches case and accents then falls back to full list`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    { "data": [], "metadata": { "count": 0, "pageSize": 20, "pageNumber": 1, "totalPages": 0 } }
                    """.trimIndent(),
                ),
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "data": [
                        { "id": 1, "nombre": "Centro" },
                        { "id": 2, "nombre": "San Francisco" }
                      ],
                      "metadata": { "count": 2, "pageSize": 100, "pageNumber": 1, "totalPages": 1 }
                    }
                    """.trimIndent(),
                ),
        )

        val result = repository.searchBarrios("centro")

        assertTrue(result is AppResult.Success)
        result as AppResult.Success
        assertEquals(listOf("Centro"), result.data.map { it.label })
        assertEquals("/barrios/search?q=centro&nombre=centro&pageSize=20&pageNumber=1", server.takeRequest().path)
        assertEquals("/barrios/search?pageSize=100&pageNumber=1", server.takeRequest().path)
    }

    @Test
    fun `searchBarrios with blank query lists barrios without q`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "data": [
                        { "id": 1, "nombre": "Centro" },
                        { "id": 2, "nombre": "San Francisco" }
                      ],
                      "metadata": {
                        "count": 2,
                        "pageSize": 100,
                        "pageNumber": 1,
                        "totalPages": 1
                      }
                    }
                    """.trimIndent(),
                ),
        )

        val result = repository.searchBarrios("  ")

        assertTrue(result is AppResult.Success)
        result as AppResult.Success
        assertEquals(listOf("Centro", "San Francisco"), result.data.map { it.label })

        val request = server.takeRequest()
        assertEquals("/barrios/search?pageSize=100&pageNumber=1", request.path)
    }

    @Test
    fun `getAllRubros lists without categoriaId`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "data": [
                        { "id": 1, "nombre": "Plomería" },
                        { "id": 2, "nombre": "Electricista" }
                      ],
                      "metadata": { "count": 2, "pageSize": 100, "pageNumber": 1, "totalPages": 1 }
                    }
                    """.trimIndent(),
                ),
        )

        val result = repository.getAllRubros()

        assertTrue(result is AppResult.Success)
        result as AppResult.Success
        assertEquals(listOf("Plomería", "Electricista"), result.data.map { it.label })
        assertEquals("/rubros/search?pageSize=100&pageNumber=1", server.takeRequest().path)

        val cached = repository.getAllRubros()
        assertTrue(cached is AppResult.Success)
        assertEquals(0, server.requestCount - 1)
    }
}
