package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.notifications.dto.NotificationDto
import java.time.Instant
import java.time.ZoneId
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationMapperTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val now = Instant.parse("2026-10-01T15:00:00Z")

    @Test
    fun `acepta titulo cuerpo tipo y booking_id del payload`() {
        val dto = json.decodeFromString(
            NotificationDto.serializer(),
            """
            {
              "id": 7,
              "tipo": "BOOKING",
              "titulo": "El trabajador está en camino",
              "cuerpo": "Ya salió hacia tu domicilio",
              "payloadJson": "{\"booking_id\": 42}",
              "leido": false,
              "createdAt": "2026-10-01T14:30:00Z"
            }
            """.trimIndent(),
        )
        val item = with(NotificationMapper) { dto.toDomain(now) }
        assertEquals("BOOKING", item.type)
        assertEquals("El trabajador está en camino", item.title)
        assertEquals("Ya salió hacia tu domicilio", item.body)
        assertEquals(42L, item.bookingId)
        assertEquals("Hace 30 min", item.createdAtLabel)
    }

    @Test
    fun `acepta el formato title body type y entityId`() {
        val dto = NotificationDto(
            id = 1,
            type = "SOLICITUD",
            title = "Pedido confirmado",
            body = "El trabajador aceptó",
            entityType = "SOLICITUD",
            entityId = 9,
            createdAt = "2026-09-01T14:00:00Z",
        )
        val item = with(NotificationMapper) { dto.toDomain(now) }
        assertEquals("Pedido confirmado", item.title)
        assertEquals("El trabajador aceptó", item.body)
        assertEquals(9L, item.orderId)
        assertEquals(
            "1 sept 2026",
            NotificationMapper.relativeTime(dto.createdAt, now, ZoneId.of("America/Argentina/Buenos_Aires")),
        )
    }
}
