package com.cocido.oficios.feature.home.providerhome

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.common.ClientHomeTabRouteHost

@Composable
fun ProviderHomeRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onNavigateToOrdersList: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToAgendaConfig: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProviderHomeEffect.NavigateToSettings -> onNavigateToSettings()
                ProviderHomeEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
                ProviderHomeEffect.NavigateToOrdersList -> onNavigateToOrdersList()
                ProviderHomeEffect.NavigateToAgenda -> onNavigateToAgenda()
                ProviderHomeEffect.NavigateToAgendaConfig -> onNavigateToAgendaConfig()
                is ProviderHomeEffect.NavigateToOrderDetail -> onNavigateToOrderDetail(effect.orderId)
                is ProviderHomeEffect.ShowMessage -> toastController.showError(effect.message)
                ProviderHomeEffect.NavigateToFavorites -> onNavigateToFavorites()
                ProviderHomeEffect.NavigateToHelp -> onNavigateToHelp()
                ProviderHomeEffect.NavigateToNotifications -> onNavigateToNotifications()
            }
        }
    }

    ClientHomeTabRouteHost(
        onNavigateToOrders = onNavigateToOrdersList,
        onNavigateToSettings = onNavigateToSettings,
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
