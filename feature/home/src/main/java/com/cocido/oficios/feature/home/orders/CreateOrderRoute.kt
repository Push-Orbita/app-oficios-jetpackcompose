package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun CreateOrderRoute(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToAddLocation: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastController = LocalOficiosToastController.current
    val sentToast = stringResource(R.string.create_order_sent_toast)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CreateOrderEffect.RequestSent -> {
                    toastController.showSuccess(sentToast)
                    onNavigateHome()
                }
                is CreateOrderEffect.ShowMessage -> onShowMessage(effect.message)
                CreateOrderEffect.NavigateToAddLocation -> onNavigateToAddLocation()
            }
        }
    }

    CreateOrderScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
    )
}
