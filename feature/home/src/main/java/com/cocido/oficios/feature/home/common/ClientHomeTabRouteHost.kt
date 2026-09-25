package com.cocido.oficios.feature.home.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cocido.oficios.feature.home.components.ClientHomeTab

/**
 * Host del bottom nav en Inicio. Pedidos, Favoritos, Ayuda y Ajustes son destinos
 * propios: este host solo muestra Inicio, así que al volver el highlight es Inicio.
 */
@Composable
fun ClientHomeTabRouteHost(
    onStay: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
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

    content(ClientHomeTab.INICIO) { tab ->
        ClientHomeTabNavigator.handleTabSelected(
            tab = tab,
            onStay = onStay,
            onNavigateToOrders = onNavigateToOrders,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToHelp = onNavigateToHelp,
            onNavigateToFavorites = onNavigateToFavorites,
        )
    }
}
