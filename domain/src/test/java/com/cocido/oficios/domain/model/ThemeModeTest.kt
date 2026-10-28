package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun lightIsNeverDark() {
        assertFalse(ThemeMode.LIGHT.isDark(systemInDarkTheme = true))
        assertFalse(ThemeMode.LIGHT.isDark(systemInDarkTheme = false))
    }

    @Test
    fun darkIsAlwaysDark() {
        assertTrue(ThemeMode.DARK.isDark(systemInDarkTheme = true))
        assertTrue(ThemeMode.DARK.isDark(systemInDarkTheme = false))
    }

    @Test
    fun systemFollowsDevice() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemInDarkTheme = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemInDarkTheme = false))
    }

    @Test
    fun parseThemeModeReadsStoredNamesAndDefaultsToLight() {
        assertEquals(ThemeMode.LIGHT, parseThemeMode("LIGHT"))
        assertEquals(ThemeMode.DARK, parseThemeMode("DARK"))
        assertEquals(ThemeMode.SYSTEM, parseThemeMode("SYSTEM"))
        assertEquals(ThemeMode.LIGHT, parseThemeMode(null))
        assertEquals(ThemeMode.LIGHT, parseThemeMode(""))
        assertEquals(ThemeMode.LIGHT, parseThemeMode("oscuro"))
    }
}
