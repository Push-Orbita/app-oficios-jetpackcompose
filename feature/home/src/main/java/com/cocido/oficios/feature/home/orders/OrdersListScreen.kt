package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosTopAppBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.OrdersListFilter
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
) {
    if (!showBackNavigation && onTabSelected == null) {
        Column(modifier = modifier.fillMaxSize()) {
            OficiosTopAppBar(title = stringResource(R.string.orders_title), windowInsets = WindowInsets(0, 0, 0, 0))
            OrdersListBody(uiState = uiState, onEvent = onEvent, modifier = Modifier.weight(1f))
        }
        return
    }
    OficiosScaffold(
        modifier = modifier,
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
    when (uiState) {
        OrdersListUiState.Loading -> Column(modifier = modifier.fillMaxSize().padding(OficiosSpacing.lg), verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            OficiosLoadingState(message = stringResource(R.string.orders_loading))
            repeat(3) { OrderSkeletonCard() }
        }
        is OrdersListUiState.Error -> OficiosErrorState(
            title = stringResource(R.string.orders_error_title),
            description = uiState.message,
            onRetry = { onEvent(OrdersListEvent.Retry) },
            modifier = modifier.fillMaxSize(),
        )
        is OrdersListUiState.Success -> Column(modifier = modifier.fillMaxSize()) {
            OrdersFilterRow(selected = uiState.filter, onSelected = { onEvent(OrdersListEvent.FilterSelected(it)) })
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onEvent(OrdersListEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (uiState.visibleOrders.isEmpty()) {
                    val history = uiState.filter == OrdersListFilter.HISTORY
                    OficiosEmptyState(
                        title = stringResource(if (history) R.string.orders_empty_history_title else R.string.orders_empty_active_title),
                        description = stringResource(if (history) R.string.orders_empty_history_description else R.string.orders_empty_active_description),
                        ctaLabel = if (history) null else stringResource(R.string.orders_empty_cta),
                        onCta = if (history) null else ({ onEvent(OrdersListEvent.SearchClicked) }),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    ) {
                        items(uiState.visibleOrders, key = { it.id }) { order ->
                            OrderListItemCard(order = order, onClick = { onEvent(OrdersListEvent.OrderClicked(order.id)) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersFilterRow(selected: OrdersListFilter, onSelected: (OrdersListFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.sm),
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
        modifier = Modifier.height(OficiosComponentSize.buttonHeight),
    )
}

@Composable
private fun OrderListItemCard(order: ServiceOrderListItem, onClick: () -> Unit) {
    OficiosCard(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun OrderSkeletonCard() {
    OficiosCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).padding(0.dp))
            Box(Modifier.fillMaxWidth().height(16.dp))
            Box(Modifier.fillMaxWidth(0.6f).height(14.dp))
        }
    }
}
