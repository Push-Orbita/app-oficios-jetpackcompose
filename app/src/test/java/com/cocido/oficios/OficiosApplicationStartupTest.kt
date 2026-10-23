package com.cocido.oficios

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.theme.OficiosSystemBars
import com.cocido.oficios.theme.ThemeNightMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Corre el [OficiosApplication.attachBaseContext] real (night mode en frío)
 * sin Hilt ni MapsPreheat: [onCreate] de Hilt exige el grafo completo.
 */
class OficiosApplicationColdStartProbe : OficiosApplication() {
    override fun onCreate() {
        ThemeNightMode.applyFromPreferences(this)
    }
}

/**
 * `getApplicationContext()` todavía no existe en attachBaseContext
 * (LoadedApk.mApplication sigue null).
 */
private class ContextWithoutApplication(base: Context) : ContextWrapper(base) {
    override fun getApplicationContext(): Context {
        throw AssertionError("applicationContext is null during Application.attachBaseContext")
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = OficiosApplicationColdStartProbe::class)
class OficiosApplicationStartupTest {

    @Test
    fun applicationProviderInstantiatesWithoutCrash() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        assertNotNull(app)
        assertTrue(app is OficiosApplication)
        assertEquals(ThemeMode.LIGHT, ThemeModePreferences.read(app))
    }

    @Test
    fun realApplicationAttachBaseContextDoesNotUseApplicationContext() {
        val app = OficiosApplication()
        val runtime = ApplicationProvider.getApplicationContext<Application>()
        val base = ContextWithoutApplication(runtime)
        val attach = OficiosApplication::class.java.getDeclaredMethod(
            "attachBaseContext",
            Context::class.java,
        )
        attach.isAccessible = true
        attach.invoke(app, base)
        assertEquals(ThemeMode.LIGHT, ThemeModePreferences.read(base))
    }

    @Test
    fun applyFromPreferencesHonorsStoredDarkMode() {
        val app = ApplicationProvider.getApplicationContext<OficiosApplicationColdStartProbe>()
        ThemeModePreferences.write(app, ThemeMode.DARK)
        ThemeNightMode.applyFromPreferences(app)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = OficiosApplicationColdStartProbe::class)
class MainActivityColdStartTest {

    @Test
    fun activityOnCreateReadsThemeWithoutCrashing() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        ThemeNightMode.applyFromPreferences(activity)
        OficiosSystemBars.applyFromPreferences(activity)
        assertEquals(ThemeMode.LIGHT, ThemeModePreferences.read(activity))
    }
}
