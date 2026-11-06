package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.core.ui.lifecycle.RefreshOnDataChange
import com.cocido.oficios.core.common.sync.DataTopic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController

@Composable
fun ProfessionalProfileRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToServices: () -> Unit = {},
    onNavigateToAvailability: () -> Unit = {},
    onNavigateToPortfolio: () -> Unit = {},
    onNavigateToDeleteAccount: () -> Unit = {},
    viewModel: ProfessionalProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    RefreshOnResume(viewModel::onResume)
    RefreshOnDataChange(DataTopic.PROFILE_STATUS) { viewModel.onResume() }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfessionalProfileEffect.NavigateBack -> onNavigateBack()
                ProfessionalProfileEffect.NavigateToServices -> onNavigateToServices()
                ProfessionalProfileEffect.NavigateToAvailability -> onNavigateToAvailability()
                ProfessionalProfileEffect.NavigateToPortfolio -> onNavigateToPortfolio()
                ProfessionalProfileEffect.NavigateToDeleteAccount -> onNavigateToDeleteAccount()
                is ProfessionalProfileEffect.ShowError -> toastController.showError(effect.message)
            }
        }
    }
    ProfessionalProfileScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
