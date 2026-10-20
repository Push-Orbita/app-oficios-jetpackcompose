package com.cocido.oficios.feature.home.common

/**
 * El gesto de Material3 deja [androidx.compose.material3.pulltorefresh.PullToRefreshState.distanceFraction]
 * en 1 y solo lo oculta si `isRefreshing` cambia. Sin red el error vuelve en el mismo frame:
 * StateFlow descarta el `true` y el indicador queda visible.
 */
object PullRefreshSettle {
    fun displayedRefreshing(refreshRequested: Boolean, isRefreshing: Boolean): Boolean =
        isRefreshing || refreshRequested

    fun shouldSettle(refreshRequested: Boolean, isRefreshing: Boolean): Boolean =
        refreshRequested && !isRefreshing
}
