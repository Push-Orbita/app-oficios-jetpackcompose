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
// BLOQUEANTE: Licencia de DIN Pro (FontFont/Monotype) y Satoshi (Fontshare/ITF)
// pendiente. Matías debe conseguir licencia de app móvil o aprobar fallback métrico
// (p. ej. Barlow/D-DIN OFL). Ver PLAN_COMPLETO_1a1.md bloqueante #1.
//
// Para agregar las fuentes cuando estén disponibles:
//   1. Colocar los .ttf/.otf en res/font/
//   2. Crear FontFamily con FontWeight.Bold / Medium / Normal
//   3. Reemplazar OficiosFontFamilyPlaceholder por ese FontFamily
//
// Escala tipográfica de Figma: 48/36/32/16 (+14 en sistema, no usado).
// Line-height: "normal" (≈1.2x), letter-spacing: 0.
// ---------------------------------------------------------------------------

/** TODO: Reemplazar con DIN Pro cuando la licencia esté disponible */
private val OficiosFontFamilyPlaceholder = FontFamily.SansSerif

/** TODO: Reemplazar con Satoshi Medium cuando la licencia esté disponible */
private val OficiosFontFamilySatoshiPlaceholder = FontFamily.SansSerif

private val OficiosFontFamily = OficiosFontFamilyPlaceholder

val OficiosTypography = Typography(
    // Título splash / logo — "Oficios Formosa" 48sp DIN Pro Bold
    // Figma line-height "normal" ≈ 58sp (1.2x)
    displayLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 58.sp,
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
    // Títulos de pantalla — "Pedido Confirmado", "Recientes" 36sp DIN Pro Bold
    // Figma line-height "normal" ≈ 43sp (1.2x)
    headlineLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 43.sp,
        letterSpacing = 0.sp,
    ),
    // Título de sección — "Trabajos realizados", "Certificado(s)" 32sp DIN Pro Bold
    // Figma line-height "normal" ≈ 38sp (1.2x)
    headlineMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
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
    // Nombre de usuario, labels de fila — "Detalle:", "Emanuel Molina" 16sp DIN Pro Bold
    // Figma line-height "normal" ≈ 19sp (1.2x), letter-spacing 0
    titleMedium = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Cuerpo principal de texto — 16sp DIN Pro Regular
    // Figma line-height "normal" ≈ 19sp (1.2x), letter-spacing 0, sin font padding
    bodyLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
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
    // Texto de botones — 16sp DIN Pro Medium
    // Figma line-height "normal" ≈ 19sp, letter-spacing 0
    labelLarge = TextStyle(
        fontFamily = OficiosFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
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

/** Label de la bottom navigation — 16sp Satoshi Medium (única excepción a DIN Pro)
 * TODO: Reemplazar OficiosFontFamilySatoshiPlaceholder cuando Satoshi esté disponible */
val OficiosBottomNavLabelStyle = TextStyle(
    fontFamily = OficiosFontFamilySatoshiPlaceholder,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.sp,
)

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosTypography", replaceWith = ReplaceWith("OficiosTypography"))
val AppTypography = OficiosTypography
