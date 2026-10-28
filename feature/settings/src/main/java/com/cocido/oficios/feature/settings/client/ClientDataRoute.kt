package com.cocido.oficios.feature.settings.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume

@Composable
fun ClientDataRoute(
    onNavigateBack: () -> Unit,
    onNavigateToVerifyPhone: (Long, String) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: ClientDataViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    RefreshOnResume(viewModel::onResume)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ClientDataEffect.NavigateBack -> onNavigateBack()
                is ClientDataEffect.VerifyPhone -> onNavigateToVerifyPhone(effect.contactId, effect.phone)
                is ClientDataEffect.ShowMessage -> toastController.showSuccess(effect.message)
            }
        }
    }
    ClientDataScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
