package com.cocido.oficios.core.designsystem.component.categories

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Tint del glifo en cards de categoría (WCAG 1.4.11, no-texto ≥ 3:1).
 *
 * - Claro: [ColorScheme.primary] sobre surface (~11:1).
 * - Oscuro: [ColorScheme.onSurface] — primary (#006DEF) sobre surface navy
 *   (#00358B) queda ~2.4:1 y no cumple.
 */
fun categoryGlyphTint(colorScheme: ColorScheme, isDark: Boolean): Color =
    if (isDark) colorScheme.onSurface else colorScheme.primary

@Composable
@ReadOnlyComposable
fun categoryGlyphTint(): Color =
    categoryGlyphTint(
        colorScheme = MaterialTheme.colorScheme,
        isDark = OficiosTheme.isDark,
    )
