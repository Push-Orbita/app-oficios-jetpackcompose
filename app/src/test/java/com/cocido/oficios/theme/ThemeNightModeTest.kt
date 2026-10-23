package com.cocido.oficios.theme

import android.app.UiModeManager
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.theme.ThemeNightMode.toAppCompatNightMode
import com.cocido.oficios.theme.ThemeNightMode.toApplicationNightMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
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

    @Test
    fun applyFromPreferencesDoesNotUseApplicationContext() {
        val runtime = ApplicationProvider.getApplicationContext<Context>()
        ThemeModePreferences.write(runtime, ThemeMode.DARK)
        val base = object : ContextWrapper(runtime) {
            override fun getApplicationContext(): Context {
                throw AssertionError("applicationContext is null during Application.attachBaseContext")
            }
        }
        ThemeNightMode.applyFromPreferences(base)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }
}
