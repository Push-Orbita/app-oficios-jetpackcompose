package com.cocido.oficios.feature.home.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume

@Composable
fun AgendaConfigRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToAvailability: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AgendaConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    RefreshOnResume(viewModel::onResume)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AgendaConfigEffect.ShowMessage -> toastController.showSuccess(effect.message)
                is AgendaConfigEffect.ShowError -> toastController.showError(effect.message)
                AgendaConfigEffect.NavigateToAvailability -> onNavigateToAvailability()
            }
        }
    }

    AgendaConfigScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
