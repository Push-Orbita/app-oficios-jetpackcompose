package com.cocido.oficios.core.designsystem.component.brand

import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashIsotypeSizeTest {

    @Test
    fun composeSplashIsotypeMatchesSystemSafeZone() {
        val width = OficiosComponentSize.authLogoIconWidthSplash.value
        val height = OficiosComponentSize.authLogoIconHeightSplash.value
        assertEquals(66f, width)
        assertEquals(120f, height)
        val halfDiagonal = sqrt(width * width + height * height) / 2f
        assertTrue(
            "esquina Compose a $halfDiagonal dp del centro debe caber en radio 80 (círculo 160dp)",
            halfDiagonal < 80f,
        )
    }
}
