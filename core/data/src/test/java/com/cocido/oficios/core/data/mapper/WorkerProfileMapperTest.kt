package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.worker.dto.CalificacionDto
import com.cocido.oficios.core.network.api.worker.dto.TrabajadorOficioPerfilCompletoDto
import com.cocido.oficios.domain.model.WorkerProfile
import kotlinx.serialization.json.Json
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

