package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.cards.AgendaAppointmentRow
import com.cocido.oficios.core.designsystem.component.chips.OficiosSegmentChip
import com.cocido.oficios.core.designsystem.component.interaction.OficiosTouchTarget
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeletonList
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.motion.oficiosAnimateItem
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.navigation.OficiosRootTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.common.time.DisplayDateTime
import com.cocido.oficios.domain.model.AgendaAppointmentTag
import com.cocido.oficios.domain.model.OrdersListFilter
import com.cocido.oficios.domain.model.OrdersListSort
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.model.agendaRubroLabel
import com.cocido.oficios.domain.model.agendaTag
import com.cocido.oficios.domain.model.agendaTimeLabel
import com.cocido.oficios.domain.model.groupAgendaAppointments
import com.cocido.oficios.domain.model.scheduledDate
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.common.ClientHomeTabNavigator
import com.cocido.oficios.feature.home.common.rememberSettledPullRefresh
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab
import com.cocido.oficios.feature.home.navigation.OrdersDestination
import java.time.LocalDate
import java.time.ZonedDateTime

@Composable
fun OrdersListScreen(
    uiState: OrdersListUiState,
    onEvent: (OrdersListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    onTabSelected: ((ClientHomeTab) -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    today: LocalDate = LocalDate.now(DisplayDateTime.ARGENTINA),
    now: ZonedDateTime = ZonedDateTime.now(DisplayDateTime.ARGENTINA),
    showFavorites: Boolean? = null,
) {
    OficiosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = {
            if (showBackNavigation) {
                OficiosBackTopBar(
                    title = stringResource(R.string.orders_title),
                    onBack = onNavigateBack,
                )
            } else {
                OficiosRootTopBar(title = stringResource(R.string.orders_title))
            }
        },
        bottomBar = {
            if (!showBackNavigation && onTabSelected != null) {
                ClientHomeBottomBar(
                    selectedTab = ClientHomeTabNavigator.tabForDestination(OrdersDestination.ordersList),
                    onTabSelected = onTabSelected,
                    showFavorites = showFavorites,
                )
            }
        },
    ) { paddingValues ->
        OrdersListBody(
            uiState = uiState,
            onEvent = onEvent,
            today = today,
            now = now,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersListBody(
    uiState: OrdersListUiState,
    onEvent: (OrdersListEvent) -> Unit,
    today: LocalDate,
    now: ZonedDateTime,
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
            is OrdersListUiState.Success -> OrdersListSuccess(
                state = state,
                onEvent = onEvent,
                today = today,
                now = now,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrdersListSuccess(
    state: OrdersListUiState.Success,
    onEvent: (OrdersListEvent) -> Unit,
    today: LocalDate,
    now: ZonedDateTime,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OrdersFilterRow(
            selectedFilter = state.filter,
            selectedSort = state.sort,
            onFilterSelected = { onEvent(OrdersListEvent.FilterSelected(it)) },
            onSortSelected = { onEvent(OrdersListEvent.SortSelected(it)) },
        )
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
                contentKey = { it.visibleOrders(now).isEmpty() },
                label = "OrdersListEmptyTransition",
            ) { current ->
                val visibleOrders = current.visibleOrders(now)
                val history = current.filter == OrdersListFilter.HISTORY
                val worker = current.viewerRole == UserRole.PROVIDER
                if (visibleOrders.isEmpty()) {
                    RefreshablePlaceholder {
                        OficiosEmptyState(
                            title = stringResource(
                                when {
                                    history -> R.string.orders_empty_history_title
                                    worker -> R.string.orders_empty_active_worker_title
                                    else -> R.string.orders_empty_active_title
                                },
                            ),
                            description = stringResource(
                                when {
                                    history -> R.string.orders_empty_history_description
                                    worker -> R.string.orders_empty_active_worker_description
                                    else -> R.string.orders_empty_active_description
                                },
                            ),
                            ctaLabel = if (history || worker) null else stringResource(R.string.orders_empty_cta),
                            onCta = if (history || worker) null else ({ onEvent(OrdersListEvent.SearchClicked) }),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                } else {
                    val groups = groupAgendaAppointments(
                        visibleOrders,
                        today,
                        sort = current.sort,
                    )
                    val undated = visibleOrders.filter { it.scheduledDate() == null }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = OficiosSpacing.OficiosScreenPadding,
                        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    ) {
                        groups.forEach { group ->
                            item(key = "h-${group.date}") {
                                Text(
                                    text = group.headerLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            items(group.items, key = { it.id }) { order ->
                                val tag = order.agendaTag(now)
                                AgendaAppointmentRow(
                                    rubro = order.agendaRubroLabel(),
                                    address = order.addressLabel.ifBlank { order.counterpartName },
                                    time = order.agendaTimeLabel(),
                                    isToday = tag == AgendaAppointmentTag.TODAY,
                                    isNow = tag == AgendaAppointmentTag.NOW,
                                    onClick = { onEvent(OrdersListEvent.OrderClicked(order.id)) },
                                    modifier = oficiosAnimateItem(),
                                    todayLabel = stringResource(R.string.provider_home_agenda_today_tag),
                                    nowLabel = stringResource(R.string.provider_home_agenda_now_tag),
                                )
                            }
                        }
                        if (undated.isNotEmpty()) {
                            item(key = "h-undated") {
                                Text(
                                    text = stringResource(R.string.orders_undated_group),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            items(undated, key = { it.id }) { order ->
                                AgendaAppointmentRow(
                                    rubro = order.agendaRubroLabel(),
                                    address = order.addressLabel.ifBlank { order.counterpartName },
                                    time = order.scheduledLabel.orEmpty(),
                                    isToday = false,
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
}

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
private fun OrdersFilterRow(
    selectedFilter: OrdersListFilter,
    selectedSort: OrdersListSort,
    onFilterSelected: (OrdersListFilter) -> Unit,
    onSortSelected: (OrdersListSort) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.xl, vertical = OficiosSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OficiosSegmentChip(
                label = stringResource(R.string.orders_filter_active),
                selected = selectedFilter == OrdersListFilter.ACTIVE,
                onClick = { onFilterSelected(OrdersListFilter.ACTIVE) },
            )
            OficiosSegmentChip(
                label = stringResource(R.string.orders_filter_history),
                selected = selectedFilter == OrdersListFilter.HISTORY,
                onClick = { onFilterSelected(OrdersListFilter.HISTORY) },
            )
        }
        OrdersSortMenu(selected = selectedSort, onSelected = onSortSelected)
    }
}

@Composable
private fun OrdersSortMenu(
    selected: OrdersListSort,
    onSelected: (OrdersListSort) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = stringResource(
        if (selected == OrdersListSort.NEWEST_FIRST) {
            R.string.orders_sort_newest
        } else {
            R.string.orders_sort_oldest
        },
    )
    val sortDescription = stringResource(R.string.orders_sort_content_description, selectedLabel)
    Box {
        OficiosTouchTarget(
            onClick = { expanded = true },
            accessibilityContentDescription = sortDescription,
            role = Role.Button,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.ChevronDown),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            OrdersSortMenuItem(
                label = stringResource(R.string.orders_sort_newest),
                selected = selected == OrdersListSort.NEWEST_FIRST,
                onClick = {
                    onSelected(OrdersListSort.NEWEST_FIRST)
                    expanded = false
                },
            )
            OrdersSortMenuItem(
                label = stringResource(R.string.orders_sort_oldest),
                selected = selected == OrdersListSort.OLDEST_FIRST,
                onClick = {
                    onSelected(OrdersListSort.OLDEST_FIRST)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun OrdersSortMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        },
        onClick = onClick,
        trailingIcon = if (selected) {
            {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.Check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            null
        },
    )
}
