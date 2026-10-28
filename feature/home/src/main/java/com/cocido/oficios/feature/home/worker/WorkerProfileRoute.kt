package com.cocido.oficios.feature.home.worker

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.common.ClientHomeTabRouteHost

@Composable
fun WorkerProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onNavigateToRate: (Long, String) -> Unit = { _, _ -> },
    onNavigateToReport: (Long, String) -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: WorkerProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    RefreshOnResume(viewModel::onResume)
    val favoriteSavedMessage = stringResource(R.string.home_worker_favorite_saved)
    val favoriteRemovedMessage = stringResource(R.string.home_worker_favorite_removed)
    val rateUnavailableMessage = stringResource(R.string.home_worker_rate_unavailable)
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
                WorkerProfileEffect.ShowRateUnavailable -> onShowMessage(rateUnavailableMessage)
            }
        }
    }

    ClientHomeTabRouteHost(
        onStay = onNavigateBack,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToFavorites = onNavigateToFavorites,
    ) { selectedTab, onTabSelected ->
        WorkerProfileScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = modifier,
        )
    }
}
