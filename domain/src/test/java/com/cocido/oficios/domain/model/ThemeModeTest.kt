package com.cocido.oficios.domain.model

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
}
