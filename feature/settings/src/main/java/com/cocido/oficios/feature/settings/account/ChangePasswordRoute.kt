package com.cocido.oficios.feature.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChangePasswordRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ChangePasswordEffect.NavigateBack -> onNavigateBack()
                is ChangePasswordEffect.Saved -> {
                    onShowMessage(effect.message)
                    onNavigateBack()
                }
            }
        }
    }
    ChangePasswordScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
