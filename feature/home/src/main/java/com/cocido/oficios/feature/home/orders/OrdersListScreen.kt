package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonList
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosTopAppBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.component.motion.oficiosAnimateItem
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.feature.home.common.rememberSettledPullRefresh
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.common.ClientHomeTabNavigator
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.navigation.OrdersDestination

@Composable
fun OrdersListScreen(
    uiState: OrdersListUiState,
    onEvent: (OrdersListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    onTabSelected: ((ClientHomeTab) -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    if (!showBackNavigation && onTabSelected == null) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                OficiosTopAppBar(title = stringResource(R.string.orders_title), windowInsets = WindowInsets(0, 0, 0, 0))
                OrdersListBody(uiState = uiState, onEvent = onEvent, modifier = Modifier.weight(1f))
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
        return
    }
    OficiosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = {
            if (showBackNavigation) {
                OficiosBackTopBar(title = stringResource(R.string.orders_title), onBack = onNavigateBack)
            } else {
                OficiosTopAppBar(title = stringResource(R.string.orders_title))
            }
        },
        bottomBar = {
            if (!showBackNavigation && onTabSelected != null) {
                ClientHomeBottomBar(
                    selectedTab = ClientHomeTabNavigator.tabForDestination(OrdersDestination.ordersList),
                    onTabSelected = onTabSelected,
                )
            }
        },
    ) { paddingValues ->
        OrdersListBody(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersListBody(
    uiState: OrdersListUiState,
    onEvent: (OrdersListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosStateContent(targetState = uiState, modifier = modifier) { state ->
        when (state) {
            OrdersListUiState.Loading -> OficiosSkeletonList(
                contentDescription = stringResource(R.string.orders_loading),
                lines = 3,
            )
            is OrdersListUiState.Error -> {
                val pullRefresh = rememberSettledPullRefresh(state.isRefreshing) {
                    onEvent(OrdersListEvent.Refresh)
                }
                PullToRefreshBox(
                    isRefreshing = pullRefresh.isRefreshing,
                    onRefresh = pullRefresh.onRefresh,
                    state = pullRefresh.state,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    RefreshablePlaceholder {
                        OficiosErrorState(
                            title = stringResource(R.string.orders_error_title),
                            description = state.message,
                            onRetry = { onEvent(OrdersListEvent.Retry) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            is OrdersListUiState.Success -> OrdersListSuccess(state = state, onEvent = onEvent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersListSuccess(
    state: OrdersListUiState.Success,
    onEvent: (OrdersListEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OrdersFilterRow(selected = state.filter, onSelected = { onEvent(OrdersListEvent.FilterSelected(it)) })
        val pullRefresh = rememberSettledPullRefresh(state.isRefreshing) {
            onEvent(OrdersListEvent.Refresh)
        }
        PullToRefreshBox(
            isRefreshing = pullRefresh.isRefreshing,
            onRefresh = pullRefresh.onRefresh,
            state = pullRefresh.state,
            modifier = Modifier.fillMaxSize(),
        ) {
            OficiosStateContent(
                targetState = state,
                contentKey = { it.visibleOrders.isEmpty() },
                label = "OrdersListEmptyTransition",
            ) { current ->
                if (current.visibleOrders.isEmpty()) {
                    val history = current.filter == OrdersListFilter.HISTORY
                    RefreshablePlaceholder {
                        OficiosEmptyState(
                            title = stringResource(if (history) R.string.orders_empty_history_title else R.string.orders_empty_active_title),
                            description = stringResource(if (history) R.string.orders_empty_history_description else R.string.orders_empty_active_description),
                            ctaLabel = if (history) null else stringResource(R.string.orders_empty_cta),
                            onCta = if (history) null else ({ onEvent(OrdersListEvent.SearchClicked) }),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = OficiosSpacing.OficiosScreenPadding,
                        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    ) {
                        items(current.visibleOrders, key = { it.id }) { order ->
                            OrderListItemCard(
                                order = order,
                                onClick = { onEvent(OrdersListEvent.OrderClicked(order.id)) },
                                modifier = oficiosAnimateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Contenido que llena el viewport y aun así participa del nested scroll del pull-to-refresh. */
@Composable
private fun RefreshablePlaceholder(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            PlaceholderPage(content = content)
        }
    }
}

@Composable
private fun LazyItemScope.PlaceholderPage(
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillParentMaxSize(),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun OrdersFilterRow(selected: OrdersListFilter, onSelected: (OrdersListFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        FilterChip(stringResource(R.string.orders_filter_active), selected == OrdersListFilter.ACTIVE) { onSelected(OrdersListFilter.ACTIVE) }
        FilterChip(stringResource(R.string.orders_filter_history), selected == OrdersListFilter.HISTORY) { onSelected(OrdersListFilter.HISTORY) }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    com.cocido.oficios.core.designsystem.component.chips.OficiosChip(
        label = label,
        onClick = onClick,
        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.heightIn(min = OficiosComponentSize.buttonHeight),
    )
}

@Composable
private fun OrderListItemCard(
    order: ServiceOrderListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                OficiosAvatar(name = order.counterpartName, imageUrl = order.avatarUrl, circular = true)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
                    Text(order.counterpartName.ifBlank { order.subRubroLabel }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    val rubro = listOf(order.rubroLabel, order.subRubroLabel).filter { it.isNotBlank() }.joinToString(" · ")
                    if (rubro.isNotBlank()) {
                        Text(rubro, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            ServiceOrderStatusLabel(status = order.status, bookingStatus = order.bookingStatus)
            order.scheduledLabel?.let { IconLine(Icons.Outlined.Schedule, it) }
            if (order.addressLabel.isNotBlank()) IconLine(Icons.Outlined.Place, order.addressLabel)
        }
    }
}

@Composable
private fun IconLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(OficiosComponentSize.iconSm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}