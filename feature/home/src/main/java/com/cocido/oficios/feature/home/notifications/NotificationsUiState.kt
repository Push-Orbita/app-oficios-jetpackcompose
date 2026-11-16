package com.cocido.oficios.feature.home.notifications

import com.cocido.oficios.domain.model.NotificationItem

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val items: List<NotificationItem> = emptyList(),
    val soloNoLeidas: Boolean = false,
    val errorMessage: String? = null,
    val hasMore: Boolean = false,
    val pageNumber: Int = 1,
) {
    val hasUnread: Boolean get() = items.any { !it.isRead }
    val showMarkAllAction: Boolean get() = hasUnread || isMarkingAllRead
    val markAllEnabled: Boolean get() = hasUnread && !isMarkingAllRead
}

sealed interface NotificationsEvent {
    data object Retry : NotificationsEvent
    data object Refresh : NotificationsEvent
    data object LoadMore : NotificationsEvent
    data object ToggleUnreadFilter : NotificationsEvent
    data object MarkAllAsRead : NotificationsEvent
    data class RetryMarkFailed(val ids: List<Long>) : NotificationsEvent
    data class NotificationClicked(val item: NotificationItem) : NotificationsEvent
}

sealed interface NotificationsEffect {
    data class NavigateToDeepLink(val route: String) : NotificationsEffect
    data class ShowMessage(val message: String) : NotificationsEffect
    data class ShowMarkAllError(val failedIds: List<Long>) : NotificationsEffect
}
