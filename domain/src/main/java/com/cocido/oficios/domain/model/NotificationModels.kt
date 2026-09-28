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
)
