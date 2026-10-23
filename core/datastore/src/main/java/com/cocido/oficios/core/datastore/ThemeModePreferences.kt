package com.cocido.oficios.core.datastore

import android.content.Context
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.model.parseThemeMode

/**
 * Espejo síncrono de la preferencia de Apariencia. DataStore no se puede leer
 * en [android.app.Application.attachBaseContext] sin bloquear el primer frame;
 * SharedPreferences sí, y alimenta night mode + system bars del arranque en frío.
 *
 * No usa [Context.getApplicationContext]: en [android.app.Application.attachBaseContext]
 * todavía es null (LoadedApk no asignó mApplication). El caller pasa `base` o el
 * Application ya attachado en [android.app.Application.onCreate].
 */
object ThemeModePreferences {
    const val PREFS_NAME = "oficios_theme_mirror"
    const val KEY_THEME_MODE = "theme_mode"

    fun read(context: Context): ThemeMode {
        val raw = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
        return parseThemeMode(raw)
    }

    fun write(context: Context, mode: ThemeMode) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
}
