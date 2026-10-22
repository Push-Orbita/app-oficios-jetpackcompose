package com.cocido.oficios.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * OficiosTheme — punto de entrada del Design System.
 *
 * Default: tema **claro** (hueso `#F4F5F7`). El oscuro es el navy de Figma Mode 1.
 *
 * Uso:
 *   OficiosTheme { /* contenido */ }
 *   OficiosTheme(darkTheme = true) { /* preview oscuro */ }
 *   OficiosTheme.colors.success
 */
object OficiosTheme {
    val colors: OficiosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalOficiosColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalOficiosDarkTheme.current
}

@Composable
fun OficiosTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val oficiosColors = if (darkTheme) DarkOficiosColors else LightOficiosColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalReducedMotion provides rememberSystemReducedMotion(),
        LocalOficiosColors provides oficiosColors,
        LocalOficiosDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OficiosTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

@Deprecated("Usar OficiosTheme", replaceWith = ReplaceWith("OficiosTheme"))
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = OficiosTheme(darkTheme = darkTheme, content = content)
