package com.cocido.oficios.feature.home.worker

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onNavigateToRate: (Long, String) -> Unit = { _, _ -> },
    onNavigateToReport: (Long, String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val favoriteSavedMessage = stringResource(R.string.home_worker_favorite_saved)
    val favoriteRemovedMessage = stringResource(R.string.home_worker_favorite_removed)
    val toastController = LocalOficiosToastController.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WorkerProfileEffect.NavigateBack -> onNavigateBack()
                is WorkerProfileEffect.NavigateToHire -> onNavigateToHire(effect.workerId)
                is WorkerProfileEffect.ShowMessage -> toastController.showError(effect.message)
                is WorkerProfileEffect.ShowFavoriteResult -> onShowMessage(
                    if (effect.added) favoriteSavedMessage else favoriteRemovedMessage,
                )
                is WorkerProfileEffect.NavigateToRate -> onNavigateToRate(effect.bookingId, effect.workerName)
                is WorkerProfileEffect.NavigateToReport -> onNavigateToReport(effect.workerId, effect.workerName)
            }
        }
    }

    WorkerProfileScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

