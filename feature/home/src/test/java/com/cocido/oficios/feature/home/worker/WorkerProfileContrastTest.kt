package com.cocido.oficios.feature.home.worker

import androidx.compose.ui.graphics.Color
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerProfileContrastTest {

    @Test
    fun `rubro and verified onSurface meet aa on the navy header`() {
        val ratio = contrast(OficiosColorWhite, OficiosColorOutlineFill)
        assertTrue("onSurface sobre surfaceContainer: $ratio", ratio >= 4.5)
    }

    @Test
    fun `light header onSurface meets aa`() {
        val ratio = contrast(OficiosColorBackground, OficiosColorWhite)
        assertTrue("onSurface claro: $ratio", ratio >= 4.5)
    }

    @Test
    fun `secondary text and gold accent stay below aa on the navy header`() {
        val secondary = contrast(OficiosColorSecondaryText, OficiosColorOutlineFill)
        val accent = contrast(OficiosColorAccent, OficiosColorOutlineFill)
        assertTrue("secundario $secondary", secondary < 4.5)
        assertTrue("acento $accent", accent < 4.5)
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
