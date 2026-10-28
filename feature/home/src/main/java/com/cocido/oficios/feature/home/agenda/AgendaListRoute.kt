package com.cocido.oficios.feature.home.agenda

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AgendaListRoute(
    onNavigateBack: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current

    RefreshOnResume(viewModel::onResume)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AgendaListEffect.ShowMessage -> toastController.showSuccess(effect.message)
                is AgendaListEffect.ShowError -> toastController.showError(effect.message)
                AgendaListEffect.NavigateToConfig -> onNavigateToConfig()
            }
        }
    }

    AgendaListScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
