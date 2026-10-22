package com.cocido.oficios.core.datastore

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
}
