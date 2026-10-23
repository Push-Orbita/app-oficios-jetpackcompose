package com.cocido.oficios.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens semánticos de Oficios (light / dark).
 *
 * El azul de marca Figma (`#00358B`) en claro se reserva para botones primarios,
 * títulos destacados, links, círculos de ícono, chip/tab seleccionado y el
 * bottom nav (íconos/labels siempre primary; seleccionado = círculo 48dp).
 * El fondo claro es hueso **`#F4F5F7`** (rango BBVA `#F5F5F2`–`#F4F5F7`).
 * El tema oscuro replica el Mode 1 actual de Figma.
 */
@Immutable
data class OficiosColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val iconContainer: Color,
    val onIconContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val accent: Color,
    val accentBorder: Color,
    val disabled: Color,
    val placeholder: Color,
    val selectedContainer: Color,
    val onSelected: Color,
    /** Texto/ícono seleccionado sobre fondo claro/superficie (no sobre fill primary). */
    val selectedOnSurface: Color,
    val unavailable: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    /** Fill de acciones secundarias (tonal) junto al contenido, no en la barra de wizard. */
    val tonalContainer: Color,
    val onTonal: Color,
    val brand: Color,
    val scrim: Color,
    val isDark: Boolean,
)

/** Fondo claro documentado: hueso `#F4F5F7`. */
val OficiosLightBackground = Color(0xFFF4F5F7)
val OficiosLightOnSurface = Color(0xFF0D1B2A)
val OficiosLightOnSurfaceVariant = Color(0xFF4A5568)
val OficiosLightOutline = Color(0xFFE2E5EA)
val OficiosLightSurfaceVariant = Color(0xFFEEF0F3)
val OficiosLightSuccess = Color(0xFF146C43)
val OficiosLightWarning = Color(0xFF8A5A00)
val OficiosLightError = Color(0xFFC62828)

val LightOficiosColors = OficiosColors(
    background = OficiosLightBackground,
    surface = Color.White,
    surfaceVariant = OficiosLightSurfaceVariant,
    onBackground = OficiosLightOnSurface,
    onSurface = OficiosLightOnSurface,
    onSurfaceVariant = OficiosLightOnSurfaceVariant,
    primary = OficiosColorBackground,
    onPrimary = OficiosColorWhite,
    primaryContainer = OficiosColorBackground,
    onPrimaryContainer = OficiosColorWhite,
    iconContainer = OficiosColorBackground,
    onIconContainer = OficiosColorWhite,
    outline = OficiosLightOutline,
    outlineVariant = Color(0xFFD5D8DE),
    success = OficiosLightSuccess,
    onSuccess = OficiosColorWhite,
    successContainer = Color(0xFFE3F4EA),
    warning = OficiosLightWarning,
    onWarning = OficiosColorWhite,
    warningContainer = Color(0xFFFFF3D6),
    error = OficiosLightError,
    onError = OficiosColorWhite,
    errorContainer = Color(0xFFFDECEA),
    danger = OficiosColorDangerFill,
    onDanger = OficiosColorWhite,
    accent = OficiosColorAccent,
    accentBorder = OficiosColorAccentBorder,
    disabled = Color(0xFF8A8F99),
    placeholder = Color(0xFF8A8F99),
    selectedContainer = OficiosColorBackground,
    onSelected = OficiosColorWhite,
    selectedOnSurface = OficiosColorBackground,
    unavailable = OficiosLightSurfaceVariant,
    inverseSurface = OficiosColorWhite,
    inverseOnSurface = OficiosColorBackground,
    tonalContainer = Color(0xFFE8EEF7),
    onTonal = OficiosColorBackground,
    brand = OficiosColorBackground,
    scrim = Color(0x99000000),
    isDark = false,
)

val DarkOficiosColors = OficiosColors(
    background = OficiosColorBackground,
    surface = OficiosColorBackground,
    surfaceVariant = OficiosColorOutlineFill,
    onBackground = OficiosColorWhite,
    onSurface = OficiosColorWhite,
    onSurfaceVariant = OficiosColorSecondaryText,
    primary = OficiosColorAction,
    onPrimary = OficiosColorWhite,
    primaryContainer = OficiosColorActionContainer,
    onPrimaryContainer = OficiosColorWhite,
    iconContainer = OficiosColorOutlineFill,
    onIconContainer = OficiosColorWhite,
    outline = OficiosColorOutlineFill,
    outlineVariant = OficiosColorOutlineFillVariant,
    success = OficiosColorSuccessText,
    onSuccess = OficiosColorWhite,
    successContainer = OficiosColorSuccessContainer,
    warning = OficiosColorAccent,
    onWarning = OficiosColorBackground,
    warningContainer = OficiosColorAccentBorder,
    error = OficiosColorAlertText,
    onError = OficiosColorWhite,
    errorContainer = OficiosColorErrorContainer,
    danger = OficiosColorDangerFill,
    onDanger = OficiosColorWhite,
    accent = OficiosColorAccent,
    accentBorder = OficiosColorAccentBorder,
    disabled = OficiosColorDisabledText,
    placeholder = OficiosColorPlaceholder,
    selectedContainer = OficiosColorWhite,
    onSelected = OficiosColorBackground,
    selectedOnSurface = OficiosColorWhite,
    unavailable = OficiosColorUnavailable,
    inverseSurface = OficiosColorWhite,
    inverseOnSurface = OficiosColorBackground,
    tonalContainer = OficiosColorActionContainer,
    onTonal = OficiosColorWhite,
    brand = OficiosColorBackground,
    scrim = Color(0xFF000000),
    isDark = true,
)

val LocalOficiosColors = staticCompositionLocalOf { LightOficiosColors }
val LocalOficiosDarkTheme = staticCompositionLocalOf { false }

/** Contenido seleccionado: `onPrimary` si va sobre fill azul; si no, `selectedOnSurface`. */
fun OficiosColors.selectedContent(onFilledPrimary: Boolean): Color =
    if (onFilledPrimary) onPrimary else selectedOnSurface
