package com.cocido.oficios.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Primitivos — fuente de verdad extraída de Figma
// ---------------------------------------------------------------------------

val OficiosColorBackground = Color(0xFF231D38)
val OficiosColorSurface = Color(0xFF2A2340)
val OficiosColorSurfaceVariant = Color(0xFF332A4E)
val OficiosColorContainer = Color(0xFF413764)
val OficiosColorOutline = Color(0xFF413764)
val OficiosColorOutlineVariant = Color(0x66413764)     // 40 % opacity

val OficiosColorPrimary = Color(0xFFA32F56)
val OficiosColorPrimaryBorder = Color(0x80D197AB)      // rgba(209,151,171,0.5)
val OficiosColorPrimaryContainer = Color(0xFF6B1E38)
val OficiosColorOnPrimaryContainer = Color(0xFFFFD9E4)

val OficiosColorWhite = Color(0xFFFFFFFF)
val OficiosColorMutedText = Color(0xFF918E9B)

// Tertiary: casco del logo — amarillo/ámbar
val OficiosColorTertiary = Color(0xFFF4A426)
val OficiosColorTertiaryContainer = Color(0xFF7A5210)

// Semánticos extra (fuera del sistema M3 estándar, para uso en componentes)
val OficiosColorSuccess = Color(0xFF4CAF7D)
val OficiosColorSuccessContainer = Color(0xFF1A3D2B)
val OficiosColorOnSuccess = Color(0xFFFFFFFF)

val OficiosColorWarning = Color(0xFFF4A426)
val OficiosColorWarningContainer = Color(0xFF7A5210)
val OficiosColorOnWarning = Color(0xFF231D38)

val OficiosColorError = Color(0xFFCF6679)
val OficiosColorErrorContainer = Color(0xFF5C1B28)

// Light scheme primitivos (sobrios, preparados para futuro)
private val OficiosColorBackgroundLight = Color(0xFFF5F3FF)
private val OficiosColorSurfaceLight = Color(0xFFFFFFFF)
private val OficiosColorSurfaceVariantLight = Color(0xFFEBE7F5)
private val OficiosColorOutlineLight = Color(0xFF8C82A8)
private val OficiosColorPrimaryLight = Color(0xFFA32F56)
private val OficiosColorSecondaryLight = Color(0xFF413764)

// ---------------------------------------------------------------------------
// Color schemes
// ---------------------------------------------------------------------------

internal val DarkColorScheme = darkColorScheme(
    background = OficiosColorBackground,
    onBackground = OficiosColorWhite,
    surface = OficiosColorSurface,
    onSurface = OficiosColorWhite,
    surfaceVariant = OficiosColorSurfaceVariant,
    onSurfaceVariant = Color(0xB3FFFFFF),               // rgba(255,255,255,0.70)
    surfaceContainer = OficiosColorContainer,
    surfaceContainerHigh = OficiosColorContainer,
    outline = OficiosColorOutline,
    outlineVariant = OficiosColorOutlineVariant,
    primary = OficiosColorPrimary,
    onPrimary = OficiosColorWhite,
    primaryContainer = OficiosColorPrimaryContainer,
    onPrimaryContainer = OficiosColorOnPrimaryContainer,
    secondary = OficiosColorWhite,
    onSecondary = OficiosColorBackground,
    secondaryContainer = OficiosColorContainer,
    onSecondaryContainer = OficiosColorWhite,
    tertiary = OficiosColorTertiary,
    onTertiary = OficiosColorBackground,
    tertiaryContainer = OficiosColorTertiaryContainer,
    onTertiaryContainer = OficiosColorTertiary,
    error = OficiosColorError,
    onError = OficiosColorWhite,
    errorContainer = OficiosColorErrorContainer,
    onErrorContainer = OficiosColorError,
    scrim = Color(0xFF000000),
    inverseSurface = OficiosColorWhite,
    inverseOnSurface = OficiosColorBackground,
    inversePrimary = OficiosColorPrimary,
)

internal val LightColorScheme = lightColorScheme(
    background = OficiosColorBackgroundLight,
    onBackground = OficiosColorBackground,
    surface = OficiosColorSurfaceLight,
    onSurface = OficiosColorBackground,
    surfaceVariant = OficiosColorSurfaceVariantLight,
    onSurfaceVariant = OficiosColorBackground,
    outline = OficiosColorOutlineLight,
    outlineVariant = OficiosColorOutlineLight.copy(alpha = 0.5f),
    primary = OficiosColorPrimaryLight,
    onPrimary = OficiosColorWhite,
    primaryContainer = OficiosColorOnPrimaryContainer,
    onPrimaryContainer = OficiosColorPrimaryContainer,
    secondary = OficiosColorSecondaryLight,
    onSecondary = OficiosColorWhite,
    tertiary = OficiosColorTertiary,
    onTertiary = OficiosColorWhite,
    error = OficiosColorError,
    onError = OficiosColorWhite,
)
