package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CounterOfferRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CounterOfferViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CounterOfferEffect.NavigateBack -> onNavigateBack()
                is CounterOfferEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    CounterOfferScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
