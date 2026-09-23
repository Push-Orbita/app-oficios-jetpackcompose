package com.cocido.oficios.feature.home.providerhome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.common.ClientHomeTabRouteHost

@Composable
fun ProviderHomeRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToPersonalData: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onNavigateToOrdersList: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onNavigateToAgenda: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProviderHomeEffect.NavigateToSettings -> onNavigateToSettings()
                ProviderHomeEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
                ProviderHomeEffect.NavigateToOrdersList -> onNavigateToOrdersList()
                ProviderHomeEffect.NavigateToAgenda -> onNavigateToAgenda()
                is ProviderHomeEffect.NavigateToOrderDetail -> onNavigateToOrderDetail(effect.orderId)
                is ProviderHomeEffect.ShowMessage -> onShowMessage(effect.message)
                ProviderHomeEffect.NavigateToProfile -> onNavigateToPersonalData()
                ProviderHomeEffect.NavigateToFavorites -> onNavigateToFavorites()
                ProviderHomeEffect.NavigateToHelp -> onNavigateToHelp()
                ProviderHomeEffect.NavigateToNotifications -> onNavigateToNotifications()
            }
        }
    }

    ClientHomeTabRouteHost(
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToPersonalData = onNavigateToPersonalData,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToFavorites = onNavigateToFavorites,
        onVisible = viewModel::refreshUnreadCount,
    ) { selectedTab, onTabSelected ->
        ProviderHomeScreen(
            uiState = uiState,
            unreadNotificationCount = unreadCount,
            selectedTab = selectedTab,
            onEvent = viewModel::onEvent,
            onTabSelected = onTabSelected,
            modifier = modifier,
        )
    }
}
