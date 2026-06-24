package com.cocido.oficios.core.network.api.profile

import com.cocido.oficios.core.network.api.profile.dto.CreateDiaLaboralRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateFullAltaRubrosSubrubrosRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateHorarioLaboralRequestDto
import com.cocido.oficios.core.network.api.profile.dto.CreateSubrubroRequestDto
import com.cocido.oficios.core.network.api.profile.dto.UpdateTrabajadorPerfilProfesionalRequestDto
import com.cocido.oficios.core.network.test.NetworkTestFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProviderProfileApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ProviderProfileApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = NetworkTestFactory.createProviderProfileApi(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `patch trabajador-oficio me sends body and parses response`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(NetworkTestFactory.loadFixture("profile/patch_trabajador_ok.json")),
        )

        val response = api.updateProfile(
            UpdateTrabajadorPerfilProfesionalRequestDto(
                descripcion = "Plomero",
                experiencia = "3 a 5 años",
            ),
        )

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/trabajador-oficio/me", request.path)
        assertEquals("Plomero", request.body.readUtf8().let { body ->
            NetworkTestFactory.json.decodeFromString<UpdateTrabajadorPerfilProfesionalRequestDto>(body).descripcion
        })
        assertEquals(1L, response.id)
        assertEquals("12345678", response.documento)
    }

    @Test
    fun `post rubro full hits correct path`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        api.createRubroFull(
            CreateFullAltaRubrosSubrubrosRequestDto(
                rubroId = 10,
                subrubros = listOf(
                    CreateSubrubroRequestDto(
                        subRubroId = 100,
                        lugarDomicilio = true,
                        lugarLocal = false,
                    ),
                ),
                diasLaborales = listOf(
                    CreateDiaLaboralRequestDto(
                        diaInicio = "LUNES",
                        diaFin = "LUNES",
                        horariosLaborales = listOf(CreateHorarioLaboralRequestDto("09:00", "18:00")),
                    ),
                ),
            ),
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/trabajador-oficio-rubro/full", request.path)
    }

    @Test
    fun `finalize perfil hits correct path`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(NetworkTestFactory.loadFixture("profile/finalize_perfil_ok.json")),
        )

        val response = api.finalizeProfile()

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/trabajador-oficio/me/finalizar-perfil", request.path)
        assertEquals("VALIDADO", response.estado)
        assertEquals(true, response.perfilCompleto)
    }
}
