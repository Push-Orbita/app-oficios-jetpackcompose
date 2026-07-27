package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OrderDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToCounterOffer: (Long) -> Unit,
    onNavigateToRate: (Long, Long, String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Reagendar/Puntuar navegan a otra pantalla y vuelven con popBackStack — el ViewModel de
    // esta ruta no se recrea, así que hay que refrescar manualmente al resumir.
    ReloadOnResume { viewModel.onEvent(OrderDetailEvent.Retry) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OrderDetailEffect.ShowMessage -> onShowMessage(effect.message)
                is OrderDetailEffect.NavigateToCounterOffer -> onNavigateToCounterOffer(effect.orderId)
                is OrderDetailEffect.NavigateToRate -> onNavigateToRate(effect.bookingId, effect.workerId, effect.workerName)
            }
        }
    }

    OrderDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
private fun ReloadOnResume(onReload: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onReload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
