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
        assertEquals("/barrios/search?q=miguel&nombre=miguel&pageSize=100&pageNumber=1", request.path)
    }

    @Test
    fun `empty server search does not fall back to a truncated catalog`() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":[],"metadata":{"count":0,"totalPages":0}}"""))
        val result = repository.searchBarrios("missing") as AppResult.Success
        assertTrue(result.data.isEmpty())
        assertEquals(1, server.requestCount)
        assertEquals("missing", server.takeRequest().requestUrl?.queryParameter("q"))
    }

    @Test
    fun `focus preview never hides a barrio on catalog page two`() = runTest {
        server.enqueue(MockResponse().setBody("""
            {"data":[{"id":100,"nombre":"Centro"}],
             "metadata":{"count":113,"pageSize":100,"pageNumber":1,"totalPages":2}}
        """.trimIndent()))
        server.enqueue(MockResponse().setBody("""
            {"data":[{"id":1,"nombre":"1 de Mayo"}],
             "metadata":{"count":1,"pageSize":100,"pageNumber":1,"totalPages":1}}
        """.trimIndent()))
        repository.searchBarrios("")
        repository.searchBarrios("  ")
        assertEquals(1, server.requestCount)
        val result = repository.searchBarrios("1 de Mayo") as AppResult.Success
        assertEquals(listOf("1 de Mayo"), result.data.map { it.label })
        assertEquals("/barrios/search?pageSize=100&pageNumber=1", server.takeRequest().path)
        val search = server.takeRequest().requestUrl!!
        assertEquals("1 de Mayo", search.queryParameter("q"))
        assertEquals("1 de Mayo", search.queryParameter("nombre"))
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `server search includes matches from both fake result pages`() = runTest {
        val firstPageData = (1..100).joinToString { """{"id":$it,"nombre":"Barrio $it"}""" }
        server.enqueue(MockResponse().setBody("""
            {"data":[$firstPageData],
             "metadata":{"count":101,"pageSize":100,"pageNumber":1,"totalPages":2}}
        """.trimIndent()))
        server.enqueue(MockResponse().setBody("""
            {"data":[{"id":101,"nombre":"Barrio Pagina Dos"}],
             "metadata":{"count":101,"pageSize":100,"pageNumber":2,"totalPages":2}}
        """.trimIndent()))
        val result = repository.searchBarrios("Barrio") as AppResult.Success
        assertEquals(101, result.data.size)
        assertEquals("Barrio Pagina Dos", result.data.last().label)
        assertEquals("1", server.takeRequest().requestUrl?.queryParameter("pageNumber"))
        assertEquals("2", server.takeRequest().requestUrl?.queryParameter("pageNumber"))
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
