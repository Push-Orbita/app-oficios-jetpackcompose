package com.cocido.oficios.feature.home.worker

import androidx.compose.runtime.Composable
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
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val ratingUnavailableMessage = stringResource(R.string.home_worker_rate_unavailable)
    val reportComingSoonMessage = stringResource(R.string.home_worker_report_coming_soon)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WorkerProfileEffect.NavigateBack -> onNavigateBack()
                is WorkerProfileEffect.NavigateToHire -> onNavigateToHire(effect.workerId)
                is WorkerProfileEffect.ShowMessage -> onShowMessage(effect.message)
                WorkerProfileEffect.ShowRatingUnavailable -> onShowMessage(ratingUnavailableMessage)
                WorkerProfileEffect.ShowReportComingSoon -> onShowMessage(reportComingSoonMessage)
            }
        }
    }

    WorkerProfileScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

