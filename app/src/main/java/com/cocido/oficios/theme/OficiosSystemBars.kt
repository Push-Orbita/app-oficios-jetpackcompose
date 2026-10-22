package com.cocido.oficios.theme

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.cocido.oficios.core.datastore.ThemeModePreferences
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.model.isDark

object OficiosSystemBars {
    private const val LightBackground = "#F4F5F7"
    private const val DarkBackground = "#00358B"

    fun applyFromPreferences(activity: ComponentActivity) {
        val mode = ThemeModePreferences.read(activity)
        apply(activity, dark = mode.isDark(activity.isSystemInDarkTheme()))
    }

    fun apply(activity: ComponentActivity, dark: Boolean) {
        val barStyle = if (dark) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        activity.enableEdgeToEdge(
            statusBarStyle = barStyle,
            navigationBarStyle = barStyle,
        )
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
        activity.window.setBackgroundDrawable(
            ColorDrawable(Color.parseColor(if (dark) DarkBackground else LightBackground)),
        )
    }
}

fun android.content.Context.isSystemInDarkTheme(): Boolean {
    val night = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return night == android.content.res.Configuration.UI_MODE_NIGHT_YES
}
