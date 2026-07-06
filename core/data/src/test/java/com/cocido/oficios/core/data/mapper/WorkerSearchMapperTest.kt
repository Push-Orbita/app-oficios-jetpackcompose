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
