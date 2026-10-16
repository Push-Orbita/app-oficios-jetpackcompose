package com.cocido.oficios.feature.home.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PullRefreshSettleTest {

    @Test
    fun `offline refresh settles the indicator when refreshing true is skipped`() {
        // Sin red el fallo vuelve en el mismo frame: el gesto ya dejó distanceFraction en 1
        // y Compose solo observa isRefreshing=false. Hay que mostrar el indicador un frame
        // para que Material3 vea el cambio y después ocultarlo.
        assertTrue(PullRefreshSettle.displayedRefreshing(refreshRequested = true, isRefreshing = false))
        assertTrue(PullRefreshSettle.shouldSettle(refreshRequested = true, isRefreshing = false))
    }

    @Test
    fun `indicator stays out while the user is still pulling`() {
        assertFalse(PullRefreshSettle.shouldSettle(refreshRequested = false, isRefreshing = false))
    }

    @Test
    fun `indicator stays while the refresh is in progress`() {
        assertFalse(PullRefreshSettle.shouldSettle(refreshRequested = true, isRefreshing = true))
    }
}
