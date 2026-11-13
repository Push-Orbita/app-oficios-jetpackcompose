package com.cocido.oficios.core.designsystem.component.categories

import com.cocido.oficios.core.designsystem.theme.DarkColorScheme
import com.cocido.oficios.core.designsystem.theme.LightColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CategoryGlyphTintTest {

    @Test
    fun lightUsesPrimaryBrandBlue() {
        val light = LightColorScheme
        assertEquals(light.primary, categoryGlyphTint(light, isDark = false))
        assertNotEquals(light.onBackground, categoryGlyphTint(light, isDark = false))
    }

    @Test
    fun darkUsesOnSurfaceForContrastOnNavySurface() {
        val dark = DarkColorScheme
        assertEquals(dark.onSurface, categoryGlyphTint(dark, isDark = true))
        assertNotEquals(dark.primary, categoryGlyphTint(dark, isDark = true))
    }
}