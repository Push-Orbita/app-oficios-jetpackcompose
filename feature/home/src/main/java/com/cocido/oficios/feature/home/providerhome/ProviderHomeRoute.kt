package com.cocido.oficios.feature.home.providerhome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun ProviderHomeRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onNavigateToOrdersList: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onNavigateToAgenda: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProviderHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(ClientHomeTab.INICIO) }
    val comingSoon = stringResource(R.string.home_coming_soon)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProviderHomeEffect.NavigateToSettings -> {
                    selectedTab = ClientHomeTab.AJUSTES
                    onNavigateToSettings()
                }
                ProviderHomeEffect.NavigateToCompleteProfile -> onNavigateToCompleteProfile()
                ProviderHomeEffect.NavigateToOrdersList -> onNavigateToOrdersList()
                ProviderHomeEffect.NavigateToAgenda -> onNavigateToAgenda()
                is ProviderHomeEffect.NavigateToOrderDetail -> onNavigateToOrderDetail(effect.orderId)
                is ProviderHomeEffect.ShowMessage -> onShowMessage(effect.message)
                ProviderHomeEffect.ShowComingSoon -> onShowMessage(comingSoon)
            }
        }
    }

    ProviderHomeScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        onEvent = viewModel::onEvent,
        onTabSelected = { tab ->
            selectedTab = tab
            when (tab) {
                ClientHomeTab.INICIO -> Unit
                ClientHomeTab.AJUSTES -> viewModel.onEvent(ProviderHomeEvent.NavigateToSettings)
                ClientHomeTab.PERFIL -> viewModel.onEvent(ProviderHomeEvent.NavigateToProfile)
                ClientHomeTab.FAVORITOS -> viewModel.onEvent(ProviderHomeEvent.NavigateToFavorites)
                ClientHomeTab.AYUDA -> viewModel.onEvent(ProviderHomeEvent.NavigateToHelp)
            }
        },
        modifier = modifier,
    )
}
