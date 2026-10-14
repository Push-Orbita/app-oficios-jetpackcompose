package com.cocido.oficios.feature.home.favorites

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosSnackbarController
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun FavoritesRoute(
    onNavigateBack: () -> Unit,
    onNavigateToWorker: (Long) -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = true,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalOficiosSnackbarController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FavoritesEffect.ShowMessage -> snackbar.showError(effect.message)
                is FavoritesEffect.NavigateToWorker -> onNavigateToWorker(effect.workerId)
                is FavoritesEffect.NavigateToHire -> onNavigateToHire(effect.workerId)
                FavoritesEffect.NavigateToSearch -> onNavigateToSearch()
            }
        }
    }

    FavoritesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        showBackNavigation = showBackNavigation,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}
