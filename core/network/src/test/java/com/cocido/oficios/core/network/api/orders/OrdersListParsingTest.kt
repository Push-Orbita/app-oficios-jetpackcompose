package com.cocido.oficios.core.network.api.orders

import com.cocido.oficios.core.network.api.catalog.dto.PaginatedResponseDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSearchPageDto
import com.cocido.oficios.core.network.api.orders.dto.SolicitudServicioSimpleDto
import com.cocido.oficios.core.network.test.NetworkTestFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdersListParsingTest {

    private val json = NetworkTestFactory.json

    @Test
    fun `lista cliente vacia no es error`() {
        val body = """{"data":[],"metadata":{"count":0,"pageSize":30,"pageNumber":1,"totalPages":0,"sortBy":"id"}}"""
        val page = json.decodeFromString<PaginatedResponseDto<SolicitudServicioSimpleDto>>(body)
        assertTrue(page.data.isEmpty())
    }

    @Test
    fun `lista cliente tolera booking y domicilio vacios`() {
        val body = """
            {
              "data": [{
                "id": 88,
                "estado": "PENDIENTE",
                "cliente": {},
                "trabajadorOficio": { "id": 9, "usuario": { "persona": { "nombres": "Ana", "apellido": "Paz" } } },
                "rubro": {},
                "subRubro": { "nombre": "Plomería" },
                "domicilio": {},
                "contacto": {},
                "booking": {}
              }],
              "metadata": { "count": 1, "pageSize": 30, "pageNumber": 1, "totalPages": 1 }
            }
        """.trimIndent()
        val page = json.decodeFromString<PaginatedResponseDto<SolicitudServicioSimpleDto>>(body)
        assertEquals(1, page.data.size)
        assertEquals(88L, page.data.first().id)
        assertEquals("Plomería", page.data.first().subRubro?.nombre)
        assertEquals("Ana", page.data.first().trabajadorOficio?.usuario?.persona?.nombres)
    }

    @Test
    fun `search trabajador desenvuelve data anidada`() {
        val body = """
            {
              "data": {
                "trabajador": { "id": 8, "estado": "PENDIENTE" },
                "solicitudes": [{ "id": 3, "estado": "ACEPTADA", "booking": {}, "domicilio": {} }]
              },
              "metadata": { "count": 1, "pageSize": 5, "pageNumber": 1, "totalPages": 1, "sortBy": "id" }
            }
        """.trimIndent()
        val page = json.decodeFromString<SolicitudServicioSearchPageDto>(body)
        val resolved = page.resolved()
        assertEquals(8L, resolved.trabajador?.id)
        assertEquals(1, resolved.solicitudes.size)
        assertEquals(3L, resolved.solicitudes.first().id)
    }
}
