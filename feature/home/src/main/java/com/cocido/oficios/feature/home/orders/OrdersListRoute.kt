package com.cocido.oficios.feature.home.orders

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.R
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
                OrdersListEffect.ShowRefreshError -> scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.orders_refresh_error))
                }
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
        snackbarHostState = snackbarHostState,
    )
}
