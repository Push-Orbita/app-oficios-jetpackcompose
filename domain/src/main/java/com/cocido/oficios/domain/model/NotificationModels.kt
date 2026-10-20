package com.cocido.oficios.domain.model

data class NotificationItem(
    val id: Long,
    val type: String,
    val title: String,
    val body: String,
    val createdAtLabel: String,
    val isRead: Boolean,
    val entityType: String?,
    val entityId: Long?,
    val orderId: Long? = null,
    val bookingId: Long? = null,
    val deepLink: String?,
)

data class NotificationPage(
    val items: List<NotificationItem>,
    val pageNumber: Int,
    val totalPages: Int?,
    val hasMore: Boolean,
    /** Total de elementos que matchean el filtro según `metadata.count` del backend. */
    val totalCount: Int? = null,
)

/** Resultado de marcar todas como leídas (el backend no tiene mark-all: se hace 1 PATCH por notificación). */
data class MarkAllNotificationsReadResult(
    val markedIds: List<Long>,
    val failedIds: List<Long>,
)
