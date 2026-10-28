package com.cocido.oficios.feature.home.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume

@Composable
fun BookingsAgendaRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSlots: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingsAgendaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::onResume)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                BookingsAgendaEffect.NavigateToSlots -> onNavigateToSlots()
                is BookingsAgendaEffect.NavigateToOrderDetail -> onNavigateToOrderDetail(effect.orderId)
                is BookingsAgendaEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    BookingsAgendaScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
