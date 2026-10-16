package com.cocido.oficios.core.network.api.orders

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SolicitudServicioSimpleDtoParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Fixture mirrors staging GET solicitud-servicio/mis-solicitudes-cliente where
     * domicilio.favorito arrives as TINYINT 1 (not JSON boolean). PII redacted.
     */
    private val stagingLikePayload = """
        {
          "data": [
            {
              "id": 5,
              "cliente": {
                "id": 7,
                "usuario": {
                  "id": 13,
                  "email": "qa@example.test",
                  "fotoPerfil": null,
                  "emailVerificado": true,
                  "persona": { "id": 13, "nombres": "QA", "apellido": "Emulator" },
                  "rolUsuario": { "id": 2, "nombre": "CLIENTE" }
                },
                "estado": "ACTIVO",
                "documento": null
              },
              "trabajadorOficio": {
                "id": 5,
                "usuario": {
                  "id": 7,
                  "email": "worker@example.test",
                  "fotoPerfil": null,
                  "emailVerificado": true,
                  "persona": { "id": 7, "nombres": "Matias", "apellido": "Britez" },
                  "rolUsuario": { "id": 1, "nombre": "TRABAJADOR_DE_OFICIOS" }
                },
                "estado": "VALIDADO",
                "documento": "43453547"
              },
              "rubro": {
                "id": 1,
                "nombre": "Plomeria",
                "plazoCancelacionHorasDefault": 24,
                "categoria": { "id": 1, "nombre": "Hogar y Construccion" }
              },
              "subRubro": { "id": 1, "nombre": "Reparacion de canerias" },
              "domicilio": {
                "id": 6,
                "nombre": "Casa QA",
                "calle": "Av. 25 de Mayo",
                "numero": "100",
                "casaTorre": null,
                "departamento": null,
                "piso": null,
                "latitud": -26.185,
                "longitud": -58.175,
                "favorito": 1
              },
              "contacto": {
                "id": 5,
                "nombre": "QA Emulator",
                "telefonoWhatsapp": "+54000000000",
                "telefonoVerificado": false,
                "email": "qa@example.test",
                "favorito": true
              },
              "estado": "PENDIENTE",
              "descripcion": "qa",
              "booking": {
                "id": 5,
                "inicioConfirmado": "2026-09-25T10:00:00.000Z",
                "finConfirmado": "2026-09-25T12:00:00.000Z",
                "tipo": "PRESUPUESTO",
                "estado": "PENDIENTE",
                "domicilioTexto": "Casa QA",
                "contactoTexto": "QA Emulator",
                "puedeCancelar": false
              }
            }
          ],
          "metadata": {
            "count": 1,
            "pageSize": 30,
            "pageNumber": 1,
            "totalPages": 1
          }
        }
    """.trimIndent()

    @Test
    fun parsesStagingPayloadWithNumericFavorito() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(SolicitudServicioSimpleDto.serializer()),
            stagingLikePayload,
        )
        assertEquals(1, page.data.size)
        assertEquals(5L, page.data.first().id)
        assertTrue(page.data.first().domicilio!!.favorito)
        assertEquals("PENDIENTE", page.data.first().estado)
    }
}