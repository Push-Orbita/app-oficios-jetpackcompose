package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.worker.dto.CalificacionDto
import com.cocido.oficios.core.network.api.worker.dto.TrabajadorOficioPerfilCompletoDto
import com.cocido.oficios.domain.model.WorkerProfile
import kotlinx.serialization.json.Json
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps worker profile dto to domain profile with specialties and availability`() {
        val dto = json.decodeFromString(
            TrabajadorOficioPerfilCompletoDto.serializer(),
            FIXTURE_PROFILE_MINIMAL_WITH_AVAILABILITY,
        )

        val profile = dto.toWorkerProfile()

        assertEquals(6L, profile.id)
        assertEquals("Matias britez 2", profile.displayName)
        assertTrue(profile.isVerified)
        assertNull(profile.reputation)

        assertEquals(2, profile.specialties.size)
        val firstSpecialty = profile.specialties.first()
        assertEquals("Peluquería canina", firstSpecialty.rubroName)
        assertTrue(firstSpecialty.servesAtHome)
        assertFalse(firstSpecialty.servesAtLocal)

        assertEquals("Lun - Vie · 09:00 - 18:00", profile.availability?.summaryLabel)
    }

    @Test
    fun `maps empty reviews page to paged reviews`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(CalificacionDto.serializer()),
            FIXTURE_REVIEWS_EMPTY_PAGE,
        )

        val paged = page.toPagedWorkerReviews(pageSize = 5)
        assertTrue(paged.items.isEmpty())
        assertFalse(paged.hasMore)
    }

    @Test
    fun `maps a review item with booking contact to domain review`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(CalificacionDto.serializer()),
            FIXTURE_REVIEWS_SINGLE_ITEM,
        )

        val paged = page.toPagedWorkerReviews(pageSize = 5)
        assertEquals(1, paged.items.size)
        val review = paged.items.first()
        assertEquals(100L, review.id)
        assertEquals(9, review.qualityScore)
        assertEquals(4, review.punctualityScore)
        assertEquals("Comentario", review.comment)
        assertEquals("Ana", review.clientLabel)
        assertEquals(formatReviewDate("2026-01-01T11:00:00Z"), review.reviewedAt)
        assertEquals(500L, review.bookingId)
    }

    @Test
    fun `la resena muestra solo el nombre y la fecha en es-AR`() {
        val zone = ZoneId.of("America/Argentina/Buenos_Aires")
        assertEquals("Ana", reviewAuthorLabel("Ana"))
        assertEquals("María G.", reviewAuthorLabel("María Gómez | Tel: +543704000000 | Email: ana@mail.com"))
        assertNull(reviewAuthorLabel("Tel: 3704000000 | Email: ana@mail.com"))
        assertEquals("01/10/2026, 11:00", formatReviewDate("2026-10-01T14:00:00.000Z", zone))
    }

    @Test
    fun `zone uses barrio and localidad and drops street`() {
        assertEquals("San Francisco, Formosa", formatWorkerZone("San Francisco", "Formosa"))
        assertEquals("Centro", formatWorkerZone("Centro", "  "))
        assertNull(formatWorkerZone(null, null))

        val dto = json.decodeFromString(
            TrabajadorOficioPerfilCompletoDto.serializer(),
            """
            {
              "id": 9,
              "usuario": {
                "email": "a@b.c",
                "emailVerificado": false,
                "persona": { "nombres": "Ana", "apellido": "Gómez" }
              },
              "estado": "PENDIENTE",
              "descripcion": "   ",
              "domicilios": [{
                "id": 1,
                "calle": "Av. 25 de Mayo",
                "numero": "100",
                "barrio": { "id": 3, "nombre": "San Francisco", "localidad": "Formosa" }
              }]
            }
            """.trimIndent(),
        )
        val profile = dto.toWorkerProfile()
        assertEquals("San Francisco, Formosa", profile.zoneLabel)
        assertNull(profile.about)
        assertNull(profile.workAddresses.first().calle)
        assertFalse(profile.isVerified)
    }

    @Test
    fun `decodes staging trabajador-oficio 11 with localidad object and tinyint favorito`() {
        val dto = json.decodeFromString(
            TrabajadorOficioPerfilCompletoDto.serializer(),
            FIXTURE_STAGING_TRABAJADOR_OFICIO_11,
        )

        val profile = dto.toWorkerProfile()

        assertEquals(11L, profile.id)
        assertEquals("QA Trabajador", profile.displayName)
        assertEquals("Plomería", profile.specialties.first().rubroName)
        assertTrue(profile.specialties.first().servesAtHome)
        assertEquals("Lun - Sab · 08:00 - 18:00", profile.availability?.summaryLabel)
        assertEquals("Villa Hermosa, Ciudad de Formosa", profile.zoneLabel)
        assertTrue(profile.isVerified)
    }

    @Test
    fun `about is shown only when descripcion is not blank`() {
        val dto = json.decodeFromString(
            TrabajadorOficioPerfilCompletoDto.serializer(),
            """
            {
              "id": 10,
              "usuario": { "email": "a@b.c", "persona": { "nombres": "Ana", "apellido": "Gómez" } },
              "estado": "VALIDADO",
              "descripcion": " Arreglo cañerías "
            }
            """.trimIndent(),
        )
        assertEquals("Arreglo cañerías", dto.toWorkerProfile().about)
        assertTrue(dto.toWorkerProfile().isVerified)
    }

    private companion object {
        private val FIXTURE_PROFILE_MINIMAL_WITH_AVAILABILITY = """
            {
              "id": 6,
              "usuario": {
                "id": 8,
                "email": "matiasbritez.sh@gmail.com",
                "fotoPerfil": null,
                "emailVerificado": true,
                "persona": { "id": 8, "nombres": "Matias", "apellido": "britez 2" },
                "rolUsuario": { "id": 1, "nombre": "TRABAJADOR_DE_OFICIOS" }
              },
              "estado": "VALIDADO",
              "documento": "43453548",
              "rubros": [
                {
                  "id": 8,
                  "rubro": { "id": 9, "nombre": "Peluquería canina", "categoria": { "id": 4, "nombre": "Mascotas" } },
                  "principal": true,
                  "subRubros": [
                    {
                      "id": 18,
                      "subRubro": { "id": 23, "nombre": "Baño y corte" },
                      "lugarDomicilio": true,
                      "lugarLocal": false,
                      "requiereTurnos": false
                    }
                  ],
                  "diasLaborales": [
                    {
                      "id": 20,
                      "diaInicio": "LUNES",
                      "diaFin": "LUNES",
                      "horariosLaborales": [{ "id": 20, "horaInicio": "09:00:00", "horaFin": "18:00:00" }]
                    },
                    {
                      "id": 24,
                      "diaInicio": "VIERNES",
                      "diaFin": "VIERNES",
                      "horariosLaborales": [{ "id": 24, "horaInicio": "09:00:00", "horaFin": "18:00:00" }]
                    }
                  ]
                },
                {
                  "id": 9,
                  "rubro": { "id": 9, "nombre": "Peluquería canina", "categoria": { "id": 4, "nombre": "Mascotas" } },
                  "principal": true,
                  "subRubros": [],
                  "diasLaborales": []
                }
              ],
              "reputacion": null,
              "certificados": [],
              "galeriaTrabajos": [],
              "domicilios": []
            }
        """.trimIndent()

        /**
         * GET trabajador-oficio/11 en staging (200). PII redactada.
         * `barrio.localidad` llega como objeto y `favorito` como TINYINT 1.
         */
        private val FIXTURE_STAGING_TRABAJADOR_OFICIO_11 = """
            {
              "id": 11,
              "usuario": {
                "id": 26,
                "email": "qa.trabajador@example.test",
                "fotoPerfil": null,
                "emailVerificado": true,
                "persona": { "id": 26, "nombres": "QA", "apellido": "Trabajador" },
                "rolUsuario": { "id": 1, "nombre": "TRABAJADOR_DE_OFICIOS" }
              },
              "estado": "VALIDADO",
              "documento": "30111222",
              "rubros": [
                {
                  "id": 11,
                  "rubro": {
                    "id": 1,
                    "nombre": "Plomería",
                    "plazoCancelacionHorasDefault": 24,
                    "categoria": { "id": 1, "nombre": "Hogar y Construcción" }
                  },
                  "principal": true,
                  "subRubros": [
                    {
                      "id": 29,
                      "subRubro": { "id": 1, "nombre": "Reparación de cañerías" },
                      "lugarDomicilio": true,
                      "lugarLocal": false,
                      "requiereTurnos": true,
                      "plazoCancelacionHoras": null,
                      "slotsTurno": []
                    }
                  ],
                  "diasLaborales": [
                    {
                      "id": 11,
                      "diaInicio": "LUNES",
                      "diaFin": "SABADO",
                      "horariosLaborales": [
                        { "id": 11, "horaInicio": "08:00:00", "horaFin": "18:00:00" }
                      ]
                    }
                  ]
                }
              ],
              "reputacion": null,
              "certificados": [],
              "galeriaTrabajos": [],
              "domicilios": [
                {
                  "id": 9,
                  "barrioId": 107,
                  "personaId": 26,
                  "persona": {
                    "id": 26,
                    "nombres": "QA",
                    "apellido": "Trabajador",
                    "usuario": {
                      "id": 26,
                      "email": "qa.trabajador@example.test",
                      "fotoPerfil": null,
                      "rolUsuario": { "id": 1, "nombre": "TRABAJADOR_DE_OFICIOS" }
                    }
                  },
                  "barrio": {
                    "id": 107,
                    "nombre": "Villa Hermosa",
                    "localidad": { "id": 4, "nombre": "Ciudad de Formosa" }
                  },
                  "nombre": "Base QA",
                  "calle": "Av. 25 de Mayo",
                  "numero": "500",
                  "casaTorre": null,
                  "departamento": null,
                  "piso": null,
                  "latitud": -26.1852,
                  "longitud": -58.1735,
                  "favorito": 1
                }
              ]
            }
        """.trimIndent()

        private val FIXTURE_REVIEWS_EMPTY_PAGE = """
            {
              "data": [],
              "metadata": { "count": 0, "pageSize": 5, "pageNumber": 1, "totalPages": 0 }
            }
        """.trimIndent()

        private val FIXTURE_REVIEWS_SINGLE_ITEM = """
            {
              "data": [
                {
                  "id": 100,
                  "booking": {
                    "id": 500,
                    "inicioConfirmado": "2026-01-01T10:00:00Z",
                    "finConfirmado": "2026-01-01T11:00:00Z",
                    "tipo": "TRABAJO",
                    "estado": "FINALIZADA",
                    "contactoTexto": "Ana"
                  },
                  "calidad": 9,
                  "puntualidad": 4,
                  "comentario": "Comentario"
                }
              ],
              "metadata": { "count": 1, "pageSize": 5, "pageNumber": 1, "totalPages": 1 }
            }
        """.trimIndent()
    }
}

