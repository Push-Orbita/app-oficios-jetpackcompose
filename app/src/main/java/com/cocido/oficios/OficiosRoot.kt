package com.cocido.oficios

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.model.isDark
import com.cocido.oficios.navigation.OficiosApp
import com.cocido.oficios.ui.MainViewModel

@Composable
fun OficiosRoot(
    initialThemeMode: ThemeMode = ThemeMode.LIGHT,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val pendingPushRoute by viewModel.pendingPushRoute.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = initialThemeMode)
    val darkTheme = themeMode.isDark(isSystemInDarkTheme())
    val view = LocalView.current

    SideEffect {
        val activity = view.context.findComponentActivity() ?: return@SideEffect
        val barStyle = if (darkTheme) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity.enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    OficiosTheme(darkTheme = darkTheme) {
        OficiosApp(
            pendingPushRoute = pendingPushRoute,
            onPushRouteConsumed = viewModel::consumePushRoute,
        )
    }
}

private fun Context.findComponentActivity(): ComponentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return current as? ComponentActivity
}
