package com.cocido.oficios.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------------
// OficiosTheme — punto de entrada del Design System
//
// Uso:
//   OficiosTheme { /* contenido de la app */ }
//
// Por defecto es dark-first tal como el diseño de Figma.
// Para preview en light: OficiosTheme(darkTheme = false) { ... }
// ---------------------------------------------------------------------------

@Composable
fun OficiosTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: el contenido se extiende bajo las barras del sistema
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Fondo de status bar y nav bar coherente con el tema oscuro
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalReducedMotion provides rememberSystemReducedMotion()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OficiosTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosTheme", replaceWith = ReplaceWith("OficiosTheme"))
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = OficiosTheme(darkTheme = darkTheme, content = content)
