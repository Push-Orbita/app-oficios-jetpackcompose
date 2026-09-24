package com.cocido.oficios.feature.home.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cocido.oficios.feature.home.components.ClientHomeTab

/**
 * Host único del bottom nav cliente/proveedor.
 * Ayuda/Favoritos/Ajustes navegan fuera: al volver, el highlight vuelve a Inicio
 * salvo que el contenido visible sea Pedidos.
 */
@Composable
fun ClientHomeTabRouteHost(
    onStay: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onVisible: () -> Unit = {},
    content: @Composable (selectedTab: ClientHomeTab, onTabSelected: (ClientHomeTab) -> Unit) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(ClientHomeTab.INICIO) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (selectedTab != ClientHomeTab.PEDIDOS) {
                    selectedTab = ClientHomeTab.INICIO
                }
                onVisible()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    content(selectedTab) { tab ->
        if (ClientHomeTabNavigator.navigationFor(tab) is ClientHomeTabNavigation.Stay) {
            selectedTab = tab
        }
        ClientHomeTabNavigator.handleTabSelected(
            tab = tab,
            onStay = onStay,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToHelp = onNavigateToHelp,
            onNavigateToFavorites = onNavigateToFavorites,
        )
    }
}
