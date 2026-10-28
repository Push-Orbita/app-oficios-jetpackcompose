package com.cocido.oficios.feature.settings.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume

@Composable
fun CommercialLocalRoute(
    onNavigateBack: () -> Unit,
    onNavigateToAddService: () -> Unit,
    viewModel: CommercialLocalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::onResume)
    val toastController = LocalOficiosToastController.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CommercialLocalEffect.NavigateBack -> onNavigateBack()
                CommercialLocalEffect.NavigateToAddService -> onNavigateToAddService()
                is CommercialLocalEffect.ShowSuccess -> toastController.showSuccess(effect.message)
                is CommercialLocalEffect.ShowError -> toastController.showError(effect.message)
            }
        }
    }
    CommercialLocalScreen(uiState = state, onEvent = viewModel::onEvent)
}
