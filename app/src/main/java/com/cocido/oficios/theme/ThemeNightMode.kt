package com.cocido.oficios.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.domain.model.ThemeMode

/**
 * Aplica el night mode del proceso según Apariencia **antes** de inflar Activity,
 * para que values / values-night del splash de sistema coincidan con la preferencia
 * y no con el tema del teléfono.
 */
object ThemeNightMode {
    fun applyFromPreferences(context: Context) {
        apply(context, ThemeModePreferences.read(context))
    }

    fun apply(context: Context, mode: ThemeMode) {
        val nightMode = mode.toAppCompatNightMode()
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = context.applicationContext.getSystemService(UiModeManager::class.java)
            uiModeManager.setApplicationNightMode(mode.toApplicationNightMode())
        }
    }

    fun ThemeMode.toAppCompatNightMode(): Int = when (this) {
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    fun ThemeMode.toApplicationNightMode(): Int = when (this) {
        ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
        ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
        ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
    }
}
