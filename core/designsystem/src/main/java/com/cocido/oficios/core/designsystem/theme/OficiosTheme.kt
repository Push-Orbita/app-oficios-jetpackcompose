package com.cocido.oficios.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

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
