package com.cocido.oficios.feature.settings.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfessionalProfileRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToServices: () -> Unit = {},
    viewModel: ProfessionalProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfessionalProfileEffect.NavigateBack -> onNavigateBack()
                is ProfessionalProfileEffect.Saved -> {
                    onShowMessage(effect.message)
                    onNavigateBack()
                }
                ProfessionalProfileEffect.NavigateToServices -> onNavigateToServices()
            }
        }
    }
    ProfessionalProfileScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
