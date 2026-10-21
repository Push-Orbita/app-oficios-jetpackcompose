package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun MisUbicacionesRoute(
    onNavigateBack: () -> Unit,
    onConfirmed: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    viewModel: MisUbicacionesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MisUbicacionesEffect.Confirmed -> onConfirmed(effect.selectedId)
                is MisUbicacionesEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    MisUbicacionesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
    )
}
