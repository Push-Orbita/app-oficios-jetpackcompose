package com.cocido.oficios.feature.home.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDeepLink: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotificationsEffect.NavigateToDeepLink -> onNavigateToDeepLink(effect.route)
                is NotificationsEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    NotificationsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
