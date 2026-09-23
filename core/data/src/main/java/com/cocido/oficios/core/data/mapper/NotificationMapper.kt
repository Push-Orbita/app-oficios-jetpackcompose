package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.notifications.dto.NotificationDto
import com.cocido.oficios.core.network.api.notifications.dto.NotificationsPageDto
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.domain.model.NotificationPage
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object NotificationMapper {

    private val displayFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-AR"))

    fun NotificationDto.toDomain(): NotificationItem {
        val read = leido == true || readAt != null
        return NotificationItem(
            id = id,
            type = type.orEmpty(),
            title = title?.takeIf { it.isNotBlank() } ?: "Notificación",
            body = body.orEmpty(),
            createdAtLabel = formatDateTime(createdAt),
            isRead = read,
            entityType = entityType,
            entityId = entityId,
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
        )
    }

    private fun formatDateTime(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return runCatching {
            OffsetDateTime.parse(raw).format(displayFormatter)
        }.getOrDefault(raw)
    }
}
