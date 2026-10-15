package com.cocido.oficios.feature.home.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.component.chips.OficiosFilterChip
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonList
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.feedback.oficiosContentPhase
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.component.motion.oficiosAnimateItem
import com.cocido.oficios.domain.model.NotificationItem
import com.cocido.oficios.feature.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onEvent: (NotificationsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.notifications_title),
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            NotificationFilterRow(
                soloNoLeidas = uiState.soloNoLeidas,
                onToggle = { onEvent(NotificationsEvent.ToggleUnreadFilter) },
            )
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onEvent(NotificationsEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
            OficiosStateContent(targetState = uiState, contentKey = { it.phase() }) { state ->
                when (state.phase()) {
                    OficiosContentPhase.Loading -> OficiosSkeletonList(
                        contentDescription = stringResource(R.string.notifications_loading),
                        showAvatar = false,
                        lines = 3,
                    )
                    OficiosContentPhase.Error -> OficiosErrorState(
                        title = stringResource(R.string.notifications_load_error),
                        description = state.errorMessage.orEmpty(),
                        onRetry = { onEvent(NotificationsEvent.Retry) },
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    )
                    OficiosContentPhase.Empty -> OficiosEmptyState(
                        title = stringResource(R.string.notifications_empty_title),
                        description = stringResource(R.string.notifications_empty_description),
                        variant = OficiosEmptyVariant.NoMessages,
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    )
                    OficiosContentPhase.Content -> NotificationsList(
                        items = state.items,
                        contentPadding = PaddingValues(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
                        onItemClick = { onEvent(NotificationsEvent.NotificationClicked(it)) },
                        onLoadMore = { onEvent(NotificationsEvent.LoadMore) },
                        hasMore = state.hasMore,
                    )
                }
            }
            }
        }
    }
}

private fun NotificationsUiState.phase(): OficiosContentPhase =
    oficiosContentPhase(isLoading = isLoading, hasError = errorMessage != null, isEmpty = items.isEmpty())

@Composable
private fun NotificationFilterRow(
    soloNoLeidas: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            OficiosFilterChip(
                label = stringResource(R.string.notifications_filter_all),
                selected = !soloNoLeidas,
                onToggle = { if (soloNoLeidas) onToggle() },
            )
            OficiosFilterChip(
                label = stringResource(R.string.notifications_filter_unread),
                selected = soloNoLeidas,
                onToggle = { if (!soloNoLeidas) onToggle() },
            )
        }
    }
}

@Composable
private fun NotificationsList(
    items: List<NotificationItem>,
    contentPadding: PaddingValues,
    onItemClick: (NotificationItem) -> Unit,
    onLoadMore: () -> Unit,
    hasMore: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        items(items, key = { it.id }) { item ->
            NotificationRow(item = item, onClick = { onItemClick(item) }, modifier = oficiosAnimateItem())
        }
        if (hasMore) {
            item(key = "load_more") {
                OficiosButton(
                    text = stringResource(R.string.notifications_load_more),
                    onClick = onLoadMore,
                    variant = OficiosButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = item.title }
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
            )
            if (!item.isRead) {
                OficiosChip(
                    label = stringResource(R.string.notifications_unread_label),
                    containerColor = OficiosColorAction,
                    contentColor = OficiosColorWhite,
                    borderColor = OficiosColorAction,
                )
            }
            if (item.body.isNotBlank()) {
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (item.createdAtLabel.isNotBlank()) {
                Text(
                    text = item.createdAtLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
