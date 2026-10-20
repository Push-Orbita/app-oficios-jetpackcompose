package com.cocido.oficios.feature.home.notifications

import com.cocido.oficios.domain.model.NotificationItem

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<NotificationItem> = emptyList(),
    val soloNoLeidas: Boolean = false,
    val errorMessage: String? = null,
    val hasMore: Boolean = false,
    val pageNumber: Int = 1,
    /** Total de no leídas según el backend (`metadata.count`), incluye las que no están cargadas. */
    val unreadTotal: Int? = null,
    val isMarkingAllRead: Boolean = false,
) {
    val canMarkAllRead: Boolean
        get() = !isMarkingAllRead && ((unreadTotal ?: 0) > 0 || items.any { !it.isRead })
}

sealed interface NotificationsEvent {
    data object Retry : NotificationsEvent
    data object Refresh : NotificationsEvent
    data object LoadMore : NotificationsEvent
    data object ToggleUnreadFilter : NotificationsEvent
    data object MarkAllReadClicked : NotificationsEvent
    data class NotificationClicked(val item: NotificationItem) : NotificationsEvent
}

sealed interface NotificationsEffect {
    data class NavigateToDeepLink(val route: String) : NotificationsEffect
    data class ShowMessage(val message: String) : NotificationsEffect
    data class ShowSuccess(val message: String) : NotificationsEffect
}
