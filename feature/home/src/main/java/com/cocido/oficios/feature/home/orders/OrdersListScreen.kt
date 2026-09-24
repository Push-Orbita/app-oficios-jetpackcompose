package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ServiceOrderListItem

@Composable
fun OrdersListScreen(
    uiState: OrdersListUiState,
    onEvent: (OrdersListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
) {
    if (!showBackNavigation) {
        OrdersListBody(uiState = uiState, onEvent = onEvent, modifier = modifier)
        return
    }
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = "Mis pedidos", onBack = onNavigateBack) },
    ) { paddingValues ->
        OrdersListBody(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun OrdersListBody(
    uiState: OrdersListUiState,
    onEvent: (OrdersListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
            OrdersListUiState.Loading -> OficiosLoadingState(
                modifier = modifier.fillMaxSize(),
                message = "Cargando pedidos…",
            )
            is OrdersListUiState.Error -> OficiosErrorState(
                title = "No pudimos cargar tus pedidos",
                description = uiState.message,
                onRetry = { onEvent(OrdersListEvent.Retry) },
                modifier = modifier.fillMaxSize(),
            )
            is OrdersListUiState.Success -> if (uiState.orders.isEmpty()) {
                OficiosEmptyState(
                    title = "Todavía no tenés pedidos",
                    description = "Cuando solicités o recibas un pedido, va a aparecer acá.",
                    icon = Icons.Outlined.Inbox,
                    modifier = modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = OficiosSpacing.lg,
                        vertical = OficiosSpacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderListItemCard(order = order, onClick = { onEvent(OrdersListEvent.OrderClicked(order.id)) })
                    }
                }
            }
    }
}

@Composable
private fun OrderListItemCard(order: ServiceOrderListItem, onClick: () -> Unit) {
    OficiosCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
            ServiceOrderStatusLabel(status = order.status, bookingStatus = order.bookingStatus)
            Text(
                text = order.counterpartName.ifBlank { order.subRubroLabel },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(text = order.subRubroLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            order.scheduledLabel?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = order.addressLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
