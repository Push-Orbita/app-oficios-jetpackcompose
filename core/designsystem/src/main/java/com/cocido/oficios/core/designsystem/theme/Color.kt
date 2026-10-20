package com.cocido.oficios.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Primitivos — fuente de verdad: handoff de diseño "Oficios Formosa" v2
// (design-tokens.css, "Mode 1" — único tema activo del Figma).
//
// NOTA (Decisión Matías, 20/10/2026): Copy tone será "usted" según Figma.
// Los strings no se modifican en este PR; se aplicará en PRs de pantallas individuales.
// ---------------------------------------------------------------------------

/** Fondo principal — Figma rgb(0,53,139) */
val OficiosColorBackground = Color(0xFF00358B)

/** Outline / fill de tarjetas — borde 1px inset en casi todos los contenedores. Figma rgb(0,77,168) */
val OficiosColorOutlineFill = Color(0xFF004DA8)
/** Divisor auth — Figma Vector 1 ~#0047A0 (356:90). */
val OficiosColorAuthDivider = Color(0xFF0047A0)
val OficiosColorOutlineFillVariant = Color(0x66004DA8) // 40 % opacity

/** Texto secundario. Un paso más claro que el Figma para AA sobre #00358B (4.7:1). */
val OficiosColorSecondaryText = Color(0xFF8EABD4)

/** Texto secundario — valor de Figma rgb(127,154,197). Contraste 3.9:1 (AA solo para texto grande).
 * TODO: Decisión pendiente: mantener #8EABD4 (AA 4.7:1) o usar valor Figma 1:1 #7F9AC5 (AA grande).
 * Ver tokens.md §1 y PLAN_COMPLETO_1a1.md bloqueante #2. */
val OficiosColorSecondaryTextFigma = Color(0xFF7F9AC5)

/** Azul de acción — CTA secundario. Figma rgb(0,109,239). El CTA dominante es blanco. */
val OficiosColorAction = Color(0xFF006DEF)

/** Casco de marca — Figma splash 16:2 y login 356:49: acento dorado rgb(248,174,26). */
val OficiosColorBrandMark = Color(0xFFF8AE1A)
val OficiosColorActionContainer = Color(0xFF003C9A) // ver "unavailable" abajo, reutilizado como contenedor oscuro

/** Estado "no disponible" (turnos, disponibilidad). Figma rgb(0,60,154) */
val OficiosColorUnavailable = Color(0xFF003C9A)

/** Acento dorado — CTA "AHORA", validaciones. Figma rgb(248,174,26), stroke rgb(228,158,27) */
val OficiosColorAccent = Color(0xFFF8AE1A)
val OficiosColorAccentBorder = Color(0xFFE49E1B)

/** Alertas / dar de baja — stroke rgb(186,99,128), texto rgb(209,151,171) */
val OficiosColorAlert = Color(0xFFBA6380)
val OficiosColorAlertText = Color(0xFFD197AB)

/** Texto deshabilitado — Figma rgb(145,142,155). Para días fuera de mes/deshabilitados. */
val OficiosColorDisabledText = Color(0xFF918E9B)

/** Borde de input con error — Figma rgb(163,47,86). */
val OficiosColorErrorBorder = Color(0xFFA32F56)

val OficiosColorWhite = Color(0xFFFFFFFF)
/** Placeholders sobre fondo — rgba(255,255,255,0.5) */
val OficiosColorPlaceholder = Color(0x80FFFFFF)

// Semánticos extra (fuera del sistema M3 estándar, para uso en componentes de estado
// que el handoff no define explícitamente, p. ej. "aceptado" en OrderCard).
val OficiosColorSuccess = Color(0xFF4CAF7D)
/** Verde claro sobre navy o tarjeta: AA para texto chico. */
val OficiosColorSuccessText = Color(0xFFB7E4C7)
val OficiosColorSuccessContainer = Color(0xFF1A3D2B)
val OficiosColorOnSuccess = Color(0xFFFFFFFF)

val OficiosColorWarning = OficiosColorAccent
val OficiosColorWarningContainer = OficiosColorAccentBorder
val OficiosColorOnWarning = OficiosColorBackground

val OficiosColorError = OficiosColorAlert
val OficiosColorErrorContainer = Color(0xFF4A2430)

// Deprecated aliases — nombres antiguos del tema violeta, mantenidos temporalmente
// para no romper referencias no migradas. Ver docs/DESIGN_ALIGNMENT.md.
@Deprecated("Usar OficiosColorOutlineFill", ReplaceWith("OficiosColorOutlineFill"))
val OficiosColorContainer = OficiosColorOutlineFill
@Deprecated("Usar OficiosColorOutlineFill", ReplaceWith("OficiosColorOutlineFill"))
val OficiosColorOutline = OficiosColorOutlineFill
@Deprecated("Usar OficiosColorOutlineFillVariant", ReplaceWith("OficiosColorOutlineFillVariant"))
val OficiosColorOutlineVariant = OficiosColorOutlineFillVariant
@Deprecated("Usar OficiosColorAction", ReplaceWith("OficiosColorAction"))
val OficiosColorPrimary = OficiosColorAction
@Deprecated("Usar OficiosColorActionContainer", ReplaceWith("OficiosColorActionContainer"))
val OficiosColorPrimaryContainer = OficiosColorActionContainer
@Deprecated("Usar OficiosColorWhite", ReplaceWith("OficiosColorWhite"))
val OficiosColorOnPrimaryContainer = OficiosColorWhite
@Deprecated("Usar OficiosColorAlertText", ReplaceWith("OficiosColorAlertText"))
val OficiosColorPrimaryBorder = OficiosColorAlertText.copy(alpha = 0.5f)
@Deprecated("Usar OficiosColorSecondaryText", ReplaceWith("OficiosColorSecondaryText"))
val OficiosColorMutedText = OficiosColorSecondaryText
@Deprecated("Usar OficiosColorAccent", ReplaceWith("OficiosColorAccent"))
val OficiosColorTertiary = OficiosColorAccent
@Deprecated("Usar OficiosColorAccentBorder", ReplaceWith("OficiosColorAccentBorder"))
val OficiosColorTertiaryContainer = OficiosColorAccentBorder
@Deprecated("Usar OficiosColorBackground", ReplaceWith("OficiosColorBackground"))
val OficiosColorSurface = OficiosColorBackground
@Deprecated("Usar OficiosColorOutlineFill", ReplaceWith("OficiosColorOutlineFill"))
val OficiosColorSurfaceVariant = OficiosColorOutlineFill

// Light scheme primitivos — el handoff no define un modo claro (single "Mode 1"),
// pero feature:settings ya expone un toggle System/Light/Dark; se arma una variante
// clara coherente con la misma familia de color para no romper esa función existente.
private val OficiosColorBackgroundLight = Color(0xFFF3F6FC)
private val OficiosColorSurfaceLight = Color(0xFFFFFFFF)
private val OficiosColorOutlineLight = Color(0xFFB9C9E4)
private val OficiosColorOnSurfaceLight = OficiosColorBackground

// ---------------------------------------------------------------------------
// Color schemes
// ---------------------------------------------------------------------------

internal val DarkColorScheme = darkColorScheme(
    background = OficiosColorBackground,
    onBackground = OficiosColorWhite,
    surface = OficiosColorBackground,
    onSurface = OficiosColorWhite,
    surfaceVariant = OficiosColorOutlineFill,
    onSurfaceVariant = OficiosColorSecondaryText,
    surfaceContainer = OficiosColorOutlineFill,
    surfaceContainerHigh = OficiosColorOutlineFill,
    outline = OficiosColorOutlineFill,
    outlineVariant = OficiosColorOutlineFillVariant,
    primary = OficiosColorAction,
    onPrimary = OficiosColorWhite,
    primaryContainer = OficiosColorActionContainer,
    onPrimaryContainer = OficiosColorWhite,
    secondary = OficiosColorWhite,
    onSecondary = OficiosColorBackground,
    secondaryContainer = OficiosColorOutlineFill,
    onSecondaryContainer = OficiosColorWhite,
    tertiary = OficiosColorAccent,
    onTertiary = OficiosColorBackground,
    tertiaryContainer = OficiosColorAccentBorder,
    onTertiaryContainer = OficiosColorAccent,
    error = OficiosColorError,
    onError = OficiosColorWhite,
    errorContainer = OficiosColorErrorContainer,
    onErrorContainer = OficiosColorAlertText,
    scrim = Color(0xFF000000),
    inverseSurface = OficiosColorWhite,
    inverseOnSurface = OficiosColorBackground,
    inversePrimary = OficiosColorAction,
)

internal val LightColorScheme = lightColorScheme(
    background = OficiosColorBackgroundLight,
    onBackground = OficiosColorOnSurfaceLight,
    surface = OficiosColorSurfaceLight,
    onSurface = OficiosColorOnSurfaceLight,
    surfaceVariant = OficiosColorSurfaceLight,
    onSurfaceVariant = OficiosColorOnSurfaceLight,
    outline = OficiosColorOutlineLight,
    outlineVariant = OficiosColorOutlineLight.copy(alpha = 0.5f),
    primary = OficiosColorAction,
    onPrimary = OficiosColorWhite,
    primaryContainer = OficiosColorOutlineLight,
    onPrimaryContainer = OficiosColorBackground,
    secondary = OficiosColorBackground,
    onSecondary = OficiosColorWhite,
    tertiary = OficiosColorAccent,
    onTertiary = OficiosColorWhite,
    error = OficiosColorError,
    onError = OficiosColorWhite,
)
