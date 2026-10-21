package com.cocido.oficios.feature.home.search

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.common.ClientHomeTabRouteHost

@Composable
fun SearchResultsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToWorker: (Long) -> Unit,
    onNavigateToHire: (Long) -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SearchResultsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterSheetState by viewModel.filterSheetState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val comingSoon = stringResource(R.string.home_coming_soon)
    val toastController = LocalOficiosToastController.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SearchResultsEffect.NavigateBack -> onNavigateBack()
                is SearchResultsEffect.NavigateToWorker -> onNavigateToWorker(effect.workerId)
                is SearchResultsEffect.NavigateToHire -> onNavigateToHire(effect.workerId)
                SearchResultsEffect.ShowComingSoon -> onShowMessage(comingSoon)
                is SearchResultsEffect.ShowMessage -> toastController.showError(effect.message)
            }
        }
    }

    ClientHomeTabRouteHost(
        onStay = onNavigateBack,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToFavorites = onNavigateToFavorites,
    ) { selectedTab, onTabSelected ->
        SearchResultsScreen(
            uiState = uiState,
            filterSheetState = filterSheetState,
            searchQuery = searchQuery,
            onEvent = viewModel::onEvent,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = modifier,
        )
    }
}
