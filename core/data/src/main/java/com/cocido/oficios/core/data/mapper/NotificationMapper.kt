package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.core.network.api.notifications.dto.NotificationDto
import com.cocido.oficios.core.network.api.notifications.dto.NotificationsPageDto
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull

object NotificationMapper {

    fun NotificationDto.toDomain(now: Instant = Instant.now()): NotificationItem {
        val read = leido == true || readAt != null || !leidoEn.isNullOrBlank()
        val payload = payloadObject(payloadJson)
        val bookingId = longField(payload, "booking_id", "bookingId")
            ?: entityId?.takeIf { entityType.equals("BOOKING", ignoreCase = true) }
        val orderId = longField(payload, "solicitud_id", "solicitudId", "order_id")
            ?: entityId?.takeIf { entityType.equals("SOLICITUD", ignoreCase = true) || entityType.equals("PEDIDO", ignoreCase = true) }
        return NotificationItem(
            id = id,
            type = type?.takeIf { it.isNotBlank() } ?: tipo.orEmpty(),
            title = title?.takeIf { it.isNotBlank() } ?: titulo?.takeIf { it.isNotBlank() } ?: "Notificación",
            body = body?.takeIf { it.isNotBlank() } ?: cuerpo.orEmpty(),
            createdAtLabel = relativeTime(createdAt, now),
            isRead = read,
            entityType = entityType,
            entityId = entityId,
            orderId = orderId,
            bookingId = bookingId,
            deepLink = deepLink,
        )
    }

    fun NotificationsPageDto.toDomain(): NotificationPage {
        val pageNumber = metadata?.pageNumber ?: 1
        val totalPages = metadata?.totalPages
        val items = data.map { it.toDomain() }
        return NotificationPage(
            items = items,
            pageNumber = pageNumber,
            totalPages = totalPages,
            hasMore = when {
                totalPages != null -> pageNumber < totalPages
                else -> items.size >= (metadata?.pageSize ?: items.size)
            },
            totalCount = metadata?.count,
        )
    }

    fun relativeTime(raw: String?, now: Instant = Instant.now(), zone: ZoneId = DisplayDateTime.ARGENTINA): String {
        if (raw.isNullOrBlank()) return ""
        val zoned = DisplayDateTime.toZonedDateTime(raw, zone) ?: return raw
        val minutes = Duration.between(zoned.toInstant(), now).toMinutes()
        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace $minutes min"
            minutes < 24 * 60 -> "Hace ${minutes / 60} h"
            minutes < 48 * 60 -> "Ayer"
            else -> DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-AR"))
                .format(zoned)
                .replace(".", "")
        }
    }

    private fun payloadObject(payload: JsonElement?): JsonObject? = when (payload) {
        is JsonObject -> payload
        is JsonPrimitive -> payload.contentOrNull?.let { raw ->
            runCatching { Json.parseToJsonElement(raw) as? JsonObject }.getOrNull()
        }
        else -> null
    }

    private fun longField(payload: JsonObject?, vararg keys: String): Long? {
        if (payload == null) return null
        return keys.firstNotNullOfOrNull { key ->
            val value = payload[key] ?: return@firstNotNullOfOrNull null
            when (value) {
                is JsonPrimitive -> value.longOrNull ?: value.contentOrNull?.toLongOrNull()
                else -> null
            }
        }
    }
}
