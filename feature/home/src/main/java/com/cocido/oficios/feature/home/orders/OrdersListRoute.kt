package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosSnackbarController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OrdersListRoute(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    onTabSelected: ((com.cocido.oficios.feature.home.components.ClientHomeTab) -> Unit)? = null,
    onNavigateToSearch: () -> Unit = {},
    viewModel: OrdersListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalOficiosSnackbarController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OrdersListEffect.NavigateToOrderDetail -> onNavigateToOrderDetail(effect.orderId)
                OrdersListEffect.NavigateToSearch -> onNavigateToSearch()
                is OrdersListEffect.ShowRefreshError -> snackbar.showError(effect.message)
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
