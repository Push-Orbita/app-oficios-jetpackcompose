package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.ui.lifecycle.RefreshOnResume
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun CreateOrderRoute(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToAddLocation: (Long?) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    pickedLocationId: Long? = null,
    onPickedLocationConsumed: () -> Unit = {},
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(viewModel::onResume)
    val toastController = LocalOficiosToastController.current
    val sentToast = stringResource(R.string.create_order_sent_toast)

    LaunchedEffect(pickedLocationId) {
        val id = pickedLocationId ?: return@LaunchedEffect
        viewModel.onEvent(CreateOrderEvent.LocationPicked(id))
        onPickedLocationConsumed()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CreateOrderEffect.RequestSent -> {
                    toastController.showSuccess(sentToast)
                    onNavigateHome()
                }
                is CreateOrderEffect.ShowMessage -> onShowMessage(effect.message)
                CreateOrderEffect.NavigateToAddLocation -> {
                    val selected = (viewModel.uiState.value as? CreateOrderUiState.Success)?.selectedDomicilioId
                    onNavigateToAddLocation(selected)
                }
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
