package com.cocido.oficios.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cocido.oficios.core.designsystem.R

// ---------------------------------------------------------------------------
// Tipografía — Oficios Formosa
//
// Fuente de diseño: DIN Pro (Bold / Medium / Regular) + Satoshi Medium (nav)
//
// Decisión (Matías, 20/10/2026): Usar D-DIN (SIL OFL) como alternativa libre
// métrica a DIN Pro comercial, y Satoshi (ITF Free Font License) para nav labels.
//
// Mapeo de pesos:
//   DIN Pro Regular → D-DIN Regular
//   DIN Pro Bold → D-DIN Bold
//   DIN Pro Medium → D-DIN Bold (Medium usado para botones/CTAs que necesitan peso visual;
//                                 Bold es apropiado ya que D-DIN no tiene peso intermedio)
//
// Licencias: Ver third_party/fonts/LICENSES.md
// Fuentes: D-DIN (SIL OFL 1.1), Satoshi (ITF FFL 2.0)
// Archivos en: core/designsystem/src/main/res/font/
//
// Escala tipográfica de Figma: 48/36/32/16 (+14 en sistema, no usado).
// Line-height: "normal" (≈1.2x), letter-spacing: 0.
// ---------------------------------------------------------------------------

/**
 * D-DIN Font Family - alternativa libre a DIN Pro.
 * SIL Open Font License 1.1 por Datto Inc.
 * Fuente: Font Squirrel / www.datto.com/fonts/d-din
 * 
 * Nota: D-DIN solo tiene Regular y Bold. Medium (W500) se mapea a Bold porque:
 * - Figma Medium se usa principalmente en botones/CTAs que necesitan destacarse
 * - Bold es apropiado para CTAs y mantiene layout 1:1 sin alterar glyph width
 * - Preferible a Regular que haría botones demasiado ligeros
 */
private val DDinFontFamily = FontFamily(
    Font(R.font.d_din_regular, FontWeight.Normal),
    Font(R.font.d_din_bold, FontWeight.Medium),   // Medium → Bold (apropiado para botones/CTAs)
    Font(R.font.d_din_bold, FontWeight.Bold)
)

/**
 * Satoshi Font Family - usado solo para labels de bottom navigation.
 * ITF Free Font License (FFL) 2.0 por Indian Type Foundry.
 * Fuente: Fontshare (www.fontshare.com/fonts/satoshi)
 */
private val SatoshiFontFamily = FontFamily(
    Font(R.font.satoshi_medium, FontWeight.Medium)
)

private val OficiosFontFamily = DDinFontFamily

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

/** Label de la bottom navigation — 16sp Satoshi Medium (única excepción a D-DIN) */
val OficiosBottomNavLabelStyle = TextStyle(
    fontFamily = SatoshiFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.sp,
)

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosTypography", replaceWith = ReplaceWith("OficiosTypography"))
val AppTypography = OficiosTypography
