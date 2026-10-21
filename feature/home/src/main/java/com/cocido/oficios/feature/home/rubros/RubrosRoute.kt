package com.cocido.oficios.feature.home.rubros

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
fun RubrosRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearchResults: (Long, String, Long, String, String?) -> Unit,
    onShowMessage: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RubrosViewModel = hiltViewModel(),
) {
    val comingSoon = stringResource(R.string.home_coming_soon)
    val toastController = LocalOficiosToastController.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RubrosEffect.NavigateBack -> onNavigateBack()
                is RubrosEffect.NavigateToSearchResults -> onNavigateToSearchResults(
                    effect.rubroId,
                    effect.rubroLabel,
                    effect.categoryId,
                    effect.categoryLabel,
                    effect.query,
                )
                RubrosEffect.NavigateToNotifications -> onNavigateToNotifications()
                RubrosEffect.ShowComingSoon -> onShowMessage(comingSoon)
                is RubrosEffect.ShowMessage -> toastController.showError(effect.message)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    ClientHomeTabRouteHost(
        onStay = onNavigateBack,
        onNavigateToOrders = onNavigateToOrders,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToFavorites = onNavigateToFavorites,
        onVisible = viewModel::refreshUnreadCount,
    ) { selectedTab, onTabSelected ->
        RubrosScreen(
            uiState = uiState,
            unreadNotificationCount = unreadCount,
            selectedTab = selectedTab,
            onEvent = viewModel::onEvent,
            onTabSelected = onTabSelected,
            modifier = modifier,
        )
    }
}
