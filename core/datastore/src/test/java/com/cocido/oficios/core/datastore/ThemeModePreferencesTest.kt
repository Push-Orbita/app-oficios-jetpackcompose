package com.cocido.oficios.core.datastore

import android.content.Context
import android.content.ContextWrapper
import com.cocido.oficios.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeModePreferencesTest {

    @Test
    fun defaultsToLightWhenEmpty() {
        val context = RuntimeEnvironment.getApplication()
        assertEquals(ThemeMode.LIGHT, ThemeModePreferences.read(context))
    }

    @Test
    fun roundTripsDarkForColdStart() {
        val context = RuntimeEnvironment.getApplication()
        ThemeModePreferences.write(context, ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, ThemeModePreferences.read(context))
    }

    @Test
    fun readAndWriteWithoutApplicationContextLikeAttachBaseContext() {
        val base = ContextWithoutApplication(RuntimeEnvironment.getApplication())
        assertEquals(ThemeMode.LIGHT, ThemeModePreferences.read(base))
        ThemeModePreferences.write(base, ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, ThemeModePreferences.read(base))
    }
}

/**
 * Reproduce [android.app.Application.attachBaseContext]: `getApplicationContext()`
 * todavía es null porque LoadedApk no asignó mApplication.
 */
internal class ContextWithoutApplication(base: Context) : ContextWrapper(base) {
    override fun getApplicationContext(): Context {
        throw AssertionError("applicationContext is null during Application.attachBaseContext")
    }
}
