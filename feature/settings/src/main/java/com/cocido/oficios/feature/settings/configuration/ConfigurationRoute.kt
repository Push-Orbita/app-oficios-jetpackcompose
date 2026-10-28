package com.cocido.oficios.feature.settings.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConfigurationRoute(
    onNavigateBack: () -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConfigurationScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                ConfigurationEvent.NavigateBack -> onNavigateBack()
                else -> viewModel.onEvent(event)
            }
        },
    )
}
