package com.cocido.oficios.feature.home.notifications

import com.cocido.oficios.core.ui.lifecycle.RefreshOnDataChange
import com.cocido.oficios.core.common.sync.DataTopic
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.feature.home.R

@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDeepLink: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::onResume)
    RefreshOnDataChange(DataTopic.NOTIFICATIONS) { viewModel.onResume() }
    val toastController = LocalOficiosToastController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val markAllErrorMessage = stringResource(R.string.notifications_mark_all_error)
    val markAllRetryLabel = stringResource(R.string.notifications_mark_all_retry)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotificationsEffect.NavigateToDeepLink -> onNavigateToDeepLink(effect.route)
                is NotificationsEffect.ShowMessage -> toastController.showError(effect.message)
                is NotificationsEffect.ShowMarkAllError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = markAllErrorMessage,
                        actionLabel = markAllRetryLabel,
                        duration = SnackbarDuration.Long,
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onEvent(NotificationsEvent.RetryMarkFailed(effect.failedIds))
                    }
                }
            }
        }
    }

    NotificationsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
