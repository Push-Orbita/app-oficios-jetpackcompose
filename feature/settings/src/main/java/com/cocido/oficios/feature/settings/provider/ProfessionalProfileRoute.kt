package com.cocido.oficios.feature.settings.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume

@Composable
fun ProfessionalProfileRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToServices: () -> Unit = {},
    onNavigateToPortfolio: () -> Unit = {},
    onNavigateToDeleteAccount: () -> Unit = {},
    viewModel: ProfessionalProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::onResume)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfessionalProfileEffect.NavigateBack -> onNavigateBack()
                ProfessionalProfileEffect.NavigateToServices -> onNavigateToServices()
                ProfessionalProfileEffect.NavigateToPortfolio -> onNavigateToPortfolio()
                ProfessionalProfileEffect.NavigateToDeleteAccount -> onNavigateToDeleteAccount()
            }
        }
    }
    ProfessionalProfileScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
