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
        assertEquals(83f, width)
        assertEquals(152f, height)
        val diagonal = sqrt(width * width + height * height)
        assertTrue("isotipo Compose $diagonal dp debe caber en el círculo 192dp", diagonal < 192f)
    }
}
