package com.cocido.oficios.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Tipografía — Oficios Formosa
//
// Fuente de diseño: DIN Pro (Bold / Medium / Regular) + Satoshi Medium (nav)
// Fallback: FontFamily.SansSerif hasta que las fuentes sean agregadas legalmente
// al proyecto (ver docs/DESIGN_SYSTEM_SPEC.md → sección Tipografía).
//
// Para agregar DIN Pro en el futuro:
//   1. Colocar los .ttf en res/font/
//   2. Crear un FontFamily con FontWeight.Bold / Medium / Normal
//   3. Reemplazar FontFamily.SansSerif por ese FontFamily aquí
// ---------------------------------------------------------------------------

private val OficiosFontFamily = FontFamily.SansSerif

val OficiosTypography = Typography(
    // Título splash / logo — "Oficios Formosa" 48sp
    displayLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp,
    ),
    // Subtítulo grande de pantalla — 40sp
    displayMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    // Títulos de pantalla — "Pedido Confirmado", "Recientes" 36sp
    headlineLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    // Título de sección — "Beneficios", "Agenda" 20sp
    titleLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    // Nombre de usuario, labels de campo activo 16sp
    titleMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Cuerpo principal de texto — 16sp Regular
    bodyLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    // Cuerpo secundario — 14sp Regular
    bodyMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Texto de botones — 16sp Medium
    labelLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Chips, badges — 12sp Medium
    labelMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    // Caption / footnote — 11sp
    labelSmall = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/** Título "Registro de oficio" — Figma 356:610 */
val OficiosRegisterTradeTitleStyle = TextStyle(
    fontFamily = OficiosFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
)

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosTypography", replaceWith = ReplaceWith("OficiosTypography"))
val AppTypography = OficiosTypography
