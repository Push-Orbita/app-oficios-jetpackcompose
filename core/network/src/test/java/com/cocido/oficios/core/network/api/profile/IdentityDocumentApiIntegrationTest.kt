package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.common.contract.DniImageUploadContract
import com.cocido.oficios.core.network.test.NetworkTestFactory
import java.io.File
import kotlinx.coroutines.test.runTest
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
    private lateinit var uploadDataSource: IdentityDocumentUploadDataSource
    private lateinit var frontFile: File
    private lateinit var backFile: File

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createIdentityDocumentApi(server)
        uploadDataSource = IdentityDocumentUploadDataSource(api)
        frontFile = File.createTempFile("dni_front", ".jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        backFile = File.createTempFile("dni_back", ".png").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
        frontFile.delete()
        backFile.delete()
    }

    @Test
    fun `upload dni sends multipart aligned with backend contract`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        uploadDataSource.uploadDniImages(frontFile.absolutePath, backFile.absolutePath)

        val request = server.takeRequest()
        val body = request.body.readUtf8()

        assertEquals("POST", request.method)
        assertEquals("/${DniImageUploadContract.ENDPOINT_PATH}", request.path)
        assertTrue(request.headers["Content-Type"]?.startsWith("multipart/") == true)
        assertTrue(body.contains("name=\"${DniImageUploadContract.PART_FRONT_FIELD}\""))
        assertTrue(body.contains("name=\"${DniImageUploadContract.PART_BACK_FIELD}\""))
        assertTrue(body.contains("filename=\"${DniImageUploadContract.FRONT_FILE_PREFIX}_"))
        assertTrue(body.contains("filename=\"${DniImageUploadContract.BACK_FILE_PREFIX}_"))
        assertTrue(body.contains("Content-Type: image/jpeg"))
        assertTrue(body.contains("Content-Type: image/png"))
    }

    @Test
    fun `upload selfie sends multipart aligned with backend contract`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val selfieFile = File.createTempFile("selfie", ".jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        uploadDataSource.uploadSelfie(selfieFile.absolutePath)

        val request = server.takeRequest()
        val body = request.body.readUtf8()

        assertEquals("POST", request.method)
        assertTrue(request.path?.startsWith("/img-documento/subir-documento-selfie") == true)
        assertTrue(body.contains("name=\"selfieArchivo\""))
        assertTrue(body.contains("filename=\"selfie_"))
        assertTrue(body.contains("Content-Type: image/jpeg"))
        selfieFile.delete()
    }
}
