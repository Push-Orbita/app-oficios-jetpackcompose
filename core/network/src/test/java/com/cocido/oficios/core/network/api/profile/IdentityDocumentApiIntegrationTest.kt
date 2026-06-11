package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.test.NetworkTestFactory
import java.io.File
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IdentityDocumentApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: IdentityDocumentApi
    private lateinit var frontFile: File
    private lateinit var backFile: File

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createIdentityDocumentApi(server)
        frontFile = File.createTempFile("dni_front", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        backFile = File.createTempFile("dni_back", ".jpg").apply { writeBytes(byteArrayOf(4, 5, 6)) }
    }

    @After
    fun tearDown() {
        server.shutdown()
        frontFile.delete()
        backFile.delete()
    }

    @Test
    fun `upload dni sends multipart request`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val mediaType = "image/*".toMediaTypeOrNull()
        api.uploadDniDocuments(
            frenteArchivo = MultipartBody.Part.createFormData(
                "frenteArchivo",
                frontFile.name,
                frontFile.asRequestBody(mediaType),
            ),
            dorsoArchivo = MultipartBody.Part.createFormData(
                "dorsoArchivo",
                backFile.name,
                backFile.asRequestBody(mediaType),
            ),
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/img-documento/subir-documento-dni", request.path)
        assertTrue(request.headers["Content-Type"]?.startsWith("multipart/") == true)
    }
}
