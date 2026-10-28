package com.cocido.oficios.feature.home.notifications

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.badges.OficiosBadge
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.chips.OficiosSegmentChip
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonList
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.feedback.oficiosContentPhase
import com.cocido.oficios.core.designsystem.component.motion.oficiosAnimateItem
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
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
                            contentPadding = OficiosSpacing.OficiosScreenPaddingAfterTopBar,
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.xl, vertical = OficiosSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OficiosSegmentChip(
            label = stringResource(R.string.notifications_filter_all),
            selected = !soloNoLeidas,
            onClick = { if (soloNoLeidas) onToggle() },
        )
        OficiosSegmentChip(
            label = stringResource(R.string.notifications_filter_unread),
            selected = soloNoLeidas,
            onClick = { if (!soloNoLeidas) onToggle() },
        )
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
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
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
    val unreadLabel = stringResource(R.string.notifications_unread_label)
    val shape = RoundedCornerShape(OficiosRadius.sm)
    val titleColor = if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val bodyColor = if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val iconTint = if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val description = buildString {
        append(item.title)
        if (!item.isRead) append(". ").append(unreadLabel)
        if (item.body.isNotBlank()) append(". ").append(item.body)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(OficiosSpacing.sm)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(notificationIconRes(item.type)),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(OficiosComponentSize.iconMd),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!item.isRead) {
                    OficiosBadge(
                        label = unreadLabel,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            }
            if (item.body.isNotBlank()) {
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = bodyColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (item.createdAtLabel.isNotBlank()) {
                Text(
                    text = item.createdAtLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
