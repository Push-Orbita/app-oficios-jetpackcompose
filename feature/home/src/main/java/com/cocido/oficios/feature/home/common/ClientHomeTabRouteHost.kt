package com.cocido.oficios.feature.home.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cocido.oficios.feature.home.components.ClientHomeTab

/**
 * Host del bottom nav. [selectedTab] pinta el ítem activo; el lambda de tabs
 * delega en [ClientHomeTabNavigator] para no duplicar destinos por pantalla.
 */
@Composable
fun ClientHomeTabRouteHost(
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onStay: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onVisible: () -> Unit = {},
    content: @Composable (selectedTab: ClientHomeTab, onTabSelected: (ClientHomeTab) -> Unit) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onVisible()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    content(selectedTab) { tab ->
        ClientHomeTabNavigator.handleTabSelected(
            tab = tab,
            onStay = onStay,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToOrders = onNavigateToOrders,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToFavorites = onNavigateToFavorites,
        )
    }
}
