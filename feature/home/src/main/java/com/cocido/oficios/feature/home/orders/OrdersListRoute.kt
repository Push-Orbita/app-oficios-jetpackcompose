package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OrdersListRoute(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    onTabSelected: ((com.cocido.oficios.feature.home.components.ClientHomeTab) -> Unit)? = null,
    viewModel: OrdersListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OrdersListEffect.NavigateToOrderDetail -> onNavigateToOrderDetail(effect.orderId)
            }
        }
    }

    OrdersListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        showBackNavigation = showBackNavigation,
        onTabSelected = onTabSelected,
    )
}
