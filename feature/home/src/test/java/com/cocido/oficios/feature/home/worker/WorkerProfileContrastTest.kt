package com.cocido.oficios.feature.home.worker

import androidx.compose.ui.graphics.Color
import com.cocido.oficios.core.designsystem.theme.DarkOficiosColors
import com.cocido.oficios.core.designsystem.theme.LightOficiosColors
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileContrastTest {

    @Test
    fun `rubro and verified onSurface meet aa on the navy header`() {
        val dark = DarkOficiosColors
        val ratio = contrast(dark.onSurface, dark.surfaceVariant)
        assertTrue("onSurface sobre surfaceVariant: $ratio", ratio >= 4.5)
    }

    @Test
    fun `light onSurface meets aa on bone background`() {
        val light = LightOficiosColors
        val ratio = contrast(light.onSurface, light.background)
        assertTrue("onSurface claro: $ratio", ratio >= 4.5)
    }

    @Test
    fun `secondary text meets aa on light and dark backgrounds`() {
        val dark = contrast(DarkOficiosColors.onSurfaceVariant, DarkOficiosColors.background)
        val light = contrast(LightOficiosColors.onSurfaceVariant, LightOficiosColors.background)
        assertTrue("secundario oscuro $dark", dark >= 4.5)
        assertTrue("secundario claro $light", light >= 4.5)
    }
}

private fun contrast(foreground: Color, background: Color): Double {
    val lighter = maxOf(foreground.relativeLuminance(), background.relativeLuminance())
    val darker = minOf(foreground.relativeLuminance(), background.relativeLuminance())
    return (lighter + 0.05) / (darker + 0.05)
}

private fun Color.relativeLuminance(): Double {
    fun channel(value: Float): Double {
        val c = value.toDouble()
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
}
