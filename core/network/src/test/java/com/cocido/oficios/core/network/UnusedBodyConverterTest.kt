package com.cocido.oficios.core.network

import com.cocido.oficios.core.network.api.account.AccountApi
import com.cocido.oficios.core.network.api.account.dto.ChangePasswordRequestDto
import com.cocido.oficios.core.network.api.notifications.NotificationsApi
import com.cocido.oficios.core.network.api.notifications.dto.RegisterDeviceTokenRequestDto
import com.cocido.oficios.core.network.api.orders.OrdersApi
import com.cocido.oficios.core.network.api.profile.ClientProfileApi
import com.cocido.oficios.core.network.api.profile.dto.CreateDomicilioRequestDto
import com.cocido.oficios.core.network.test.NetworkTestFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.http.DELETE
import retrofit2.http.Path

class UnusedBodyConverterTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `delete contacto 200 empty body is success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))
        NetworkTestFactory.createClientProfileApi(server).deleteContacto(11L)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/contacto/11", request.path)
    }

    @Test
    fun `delete contacto 200 plain text is success`() = runTest {
        enqueuePlain(200, "Contacto eliminado correctamente")
        NetworkTestFactory.createClientProfileApi(server).deleteContacto(11L)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test
    fun `delete domicilio 200 plain text is success`() = runTest {
        enqueuePlain(200, "Domicilio eliminado correctamente")
        NetworkTestFactory.createClientProfileApi(server).deleteDomicilio(4L)
    }

    @Test
    fun `create domicilio 200 plain text is success`() = runTest {
        enqueuePlain(200, "Domicilio creado")
        NetworkTestFactory.createClientProfileApi(server).createDomicilio(
            CreateDomicilioRequestDto(barrioId = 1, personaId = 2, calle = "San Martín", numero = "1"),
        )
    }

    @Test
    fun `change password 200 plain text is success`() = runTest {
        enqueuePlain(200, "Contraseña actualizada")
        NetworkTestFactory.createRetrofit(server).create(AccountApi::class.java)
            .changePassword(ChangePasswordRequestDto("a@b.com", "secret1", "secret1"))
    }

    @Test
    fun `register device token 201 empty is success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201))
        NetworkTestFactory.createNotificationsApi(server).registerDeviceToken(
            RegisterDeviceTokenRequestDto(fcmToken = "token", platform = "android"),
        )
    }

    @Test
    fun `aceptar contrapropuesta 200 plain text is success`() = runTest {
        enqueuePlain(200, "Contrapropuesta aceptada")
        NetworkTestFactory.createOrdersApi(server).aceptarContrapropuesta(8L)
    }

    @Test
    fun `json element leftover still accepts plain text 2xx`() = runTest {
        enqueuePlain(200, "Contacto eliminado correctamente")
        val api = NetworkTestFactory.createRetrofit(server).create(LeftoverJsonElementApi::class.java)
        val body = api.delete(11L)
        assertTrue(body is JsonPrimitive)
        assertEquals("Contacto eliminado correctamente", (body as JsonPrimitive).content)
    }

    @Test
    fun `delete contacto 400 still fails`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"message":"No es su contacto"}"""),
        )
        try {
            NetworkTestFactory.createClientProfileApi(server).deleteContacto(11L)
            fail("expected HttpException")
        } catch (_: HttpException) {
            // expected
        }
    }

    private fun enqueuePlain(code: Int, body: String) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "text/plain")
                .setBody(body),
        )
    }
}

private interface LeftoverJsonElementApi {
    @DELETE("contacto/{id}")
    suspend fun delete(@Path("id") id: Long): JsonElement
}
