package com.cocido.oficios.theme

import android.app.UiModeManager
import androidx.appcompat.app.AppCompatDelegate
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.theme.ThemeNightMode.toAppCompatNightMode
import com.cocido.oficios.theme.ThemeNightMode.toApplicationNightMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeNightModeTest {

    @Test
    fun lightForcesDayResources() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeMode.LIGHT.toAppCompatNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_NO, ThemeMode.LIGHT.toApplicationNightMode())
    }

    @Test
    fun darkForcesNightResources() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeMode.DARK.toAppCompatNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_YES, ThemeMode.DARK.toApplicationNightMode())
    }

    @Test
    fun systemFollowsDevice() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeMode.SYSTEM.toAppCompatNightMode())
        assertEquals(UiModeManager.MODE_NIGHT_AUTO, ThemeMode.SYSTEM.toApplicationNightMode())
    }
}
