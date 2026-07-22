package com.cocido.oficios.feature.home.worker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WorkerProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WorkerProfileEffect.NavigateBack -> onNavigateBack()
                is WorkerProfileEffect.NavigateToHire -> onNavigateToHire(effect.workerId)
                is WorkerProfileEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    WorkerProfileScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

