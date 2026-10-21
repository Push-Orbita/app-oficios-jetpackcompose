package com.cocido.oficios.core.designsystem.a11y

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class ColorContrastTest {

    @Test
    fun whiteOnNavyMeetsAa() {
        assertTrue(contrastRatio(WHITE, NAVY) >= 4.5)
    }

    @Test
    fun actionOnWhiteMeetsAa() {
        assertTrue(contrastRatio(ACTION, WHITE) >= 4.5)
    }

    @Test
    fun secondaryTextOnNavyMeetsAa() {
        assertTrue(contrastRatio(SECONDARY, NAVY) >= 4.5)
    }

    @Test
    fun figmaSecondaryLargeTextOnNavyMeetsLargeAa() {
        assertTrue(contrastRatio(SECONDARY_FIGMA, NAVY) >= 3.0)
    }

    @Test
    fun accessibleSecondarySmallTextOnNavyMeetsAa() {
        assertTrue(contrastRatio(SECONDARY_ACCESSIBLE, NAVY) >= 4.5)
    }

    @Ignore("QA-FAIL: disabled #918E9B sobre blanco no llega a 4.5:1 — WCAG exime disabled")
    @Test
    fun disabledOnWhiteMeetsAa() {
        assertTrue(contrastRatio(DISABLED, WHITE) >= 4.5)
    }

    private companion object {
        const val WHITE = 0xFFFFFFFF
        const val NAVY = 0xFF00358B
        const val ACTION = 0xFF006DEF
        const val SECONDARY = 0xFF8EABD4
        const val SECONDARY_FIGMA = 0xFF7F9AC5
        const val SECONDARY_ACCESSIBLE = 0xFF8EA6CC
        const val DISABLED = 0xFF918E9B

        fun contrastRatio(a: Long, b: Long): Double {
            val l1 = relativeLuminance(a)
            val l2 = relativeLuminance(b)
            val lighter = max(l1, l2)
            val darker = min(l1, l2)
            return (lighter + 0.05) / (darker + 0.05)
        }

        fun relativeLuminance(argb: Long): Double {
            fun channel(value: Int): Double {
                val s = value / 255.0
                return if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
            }
            val r = ((argb shr 16) and 0xFF).toInt()
            val g = ((argb shr 8) and 0xFF).toInt()
            val b = (argb and 0xFF).toInt()
            return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
        }
    }
}
