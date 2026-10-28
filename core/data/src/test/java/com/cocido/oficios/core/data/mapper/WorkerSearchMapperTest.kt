package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.search.dto.WorkerSearchItemDto
import com.cocido.oficios.domain.model.WorkerSearchFilters
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerSearchMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `fixture maps to worker summary`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(WorkerSearchItemDto.serializer()),
            FIXTURE,
        )

        val paged = page.toPagedWorkers(WorkerSearchFilters(rubroId = 3L))

        assertEquals(1, paged.items.size)
        val worker = paged.items.first()
        assertEquals(12L, worker.id)
        assertEquals("Carlos Rodríguez", worker.displayName)
        assertEquals(4.8f, worker.rating)
        assertEquals(24, worker.reviewCount)
        assertTrue(worker.isVerified)
        assertEquals("Plomería · 5 años", worker.specialtyLabel)
    }

    @Test
    fun `maps profile photo url from usuario`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(WorkerSearchItemDto.serializer()),
            FIXTURE_WITH_PHOTO,
        )

        val worker = page.toPagedWorkers(WorkerSearchFilters()).items.first()

        assertEquals("https://cdn.example.com/avatar.jpg", worker.avatarUrl)
        assertEquals(1, worker.reviewCount)
    }

    @Test
    fun `maps puntajeGlobal when promedio is missing`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(WorkerSearchItemDto.serializer()),
            """
            {
              "data": [{
                "id": 12,
                "estado": "VALIDADO",
                "usuario": { "id": 88, "email": "a@b.c", "emailVerificado": true, "persona": { "nombres": "Ana", "apellido": "Gómez" } },
                "rubros": [],
                "reputacion": { "puntajeGlobal": 8.4, "comentariosTotales": 5 }
              }],
              "metadata": { "pageNumber": 1, "totalPages": 1, "pageSize": 20, "count": 1 }
            }
            """.trimIndent(),
        )
        val worker = page.toPagedWorkers(WorkerSearchFilters()).items.first()
        assertEquals(4.2f, worker.rating!!, 0.01f)
        assertEquals(5, worker.reviewCount)
    }

    @Test
    fun `shows no rating when reputacion fields are null`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(WorkerSearchItemDto.serializer()),
            """
            {
              "data": [{
                "id": 12,
                "estado": "VALIDADO",
                "usuario": { "id": 88, "email": "a@b.c", "emailVerificado": true, "persona": { "nombres": "Test", "apellido": "User" } },
                "rubros": [],
                "reputacion": { "promedio": null, "puntajeGlobal": null, "cantidadCalificaciones": 0 }
              }],
              "metadata": { "pageNumber": 1, "totalPages": 1, "pageSize": 20, "count": 1 }
            }
            """.trimIndent(),
        )
        val worker = page.toPagedWorkers(WorkerSearchFilters()).items.first()
        assertEquals(null, worker.rating)
        assertEquals(0, worker.reviewCount)
    }

    @Test
    fun `shows no rating when reputacion is null`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(WorkerSearchItemDto.serializer()),
            """
            {
              "data": [{
                "id": 12,
                "estado": "VALIDADO",
                "usuario": { "id": 88, "email": "a@b.c", "emailVerificado": true, "persona": { "nombres": "Test", "apellido": "User" } },
                "rubros": [],
                "reputacion": null
              }],
              "metadata": { "pageNumber": 1, "totalPages": 1, "pageSize": 20, "count": 1 }
            }
            """.trimIndent(),
        )
        val worker = page.toPagedWorkers(WorkerSearchFilters()).items.first()
        assertEquals(null, worker.rating)
        assertEquals(null, worker.reviewCount)
    }

    @Test
    fun `maps rating field as fallback when promedio and puntajeGlobal are missing`() {
        val page = json.decodeFromString(
            PaginatedResponseDto.serializer(WorkerSearchItemDto.serializer()),
            """
            {
              "data": [{
                "id": 12,
                "estado": "VALIDADO",
                "usuario": { "id": 88, "email": "a@b.c", "emailVerificado": true, "persona": { "nombres": "Matias", "apellido": "Britez" } },
                "rubros": [{ "id": 1, "principal": true, "rubro": { "id": 9, "nombre": "Reparación de cañerías" }, "subRubros": [] }],
                "reputacion": { "rating": 4.5, "reviewCount": 10 }
              }],
              "metadata": { "pageNumber": 1, "totalPages": 1, "pageSize": 20, "count": 1 }
            }
            """.trimIndent(),
        )
        val worker = page.toPagedWorkers(WorkerSearchFilters()).items.first()
        assertEquals(4.5f, worker.rating!!, 0.01f)
        assertEquals(10, worker.reviewCount)
    }

    private companion object {
        val FIXTURE = """
            {
              "data": [
                {
                  "id": 12,
                  "estado": "VALIDADO",
                  "experiencia": "5 años",
                  "usuario": {
                    "id": 88,
                    "email": "carlos@example.com",
                    "emailVerificado": true,
                    "persona": {
                      "nombres": "Carlos",
                      "apellido": "Rodríguez"
                    }
                  },
                  "rubros": [
                    {
                      "id": 1,
                      "principal": true,
                      "rubro": { "id": 3, "nombre": "Plomería" },
                      "subRubros": []
                    }
                  ],
                  "reputacion": { "promedio": 4.8, "cantidadCalificaciones": 24 }
                }
              ],
              "metadata": { "pageNumber": 1, "totalPages": 1, "pageSize": 20, "count": 1 }
            }
        """.trimIndent()

        val FIXTURE_WITH_PHOTO = """
            {
              "data": [
                {
                  "id": 12,
                  "estado": "VALIDADO",
                  "usuario": {
                    "id": 88,
                    "email": "carlos@example.com",
                    "emailVerificado": true,
                    "fotoPerfil": "https://cdn.example.com/avatar.jpg",
                    "persona": {
                      "nombres": "Carlos",
                      "apellido": "Rodríguez"
                    }
                  },
                  "rubros": [],
                  "reputacion": { "promedio": 4.8, "cantidadCalificaciones": 1 }
                }
              ],
              "metadata": { "pageNumber": 1, "totalPages": 1, "pageSize": 20, "count": 1 }
            }
        """.trimIndent()
    }
}
