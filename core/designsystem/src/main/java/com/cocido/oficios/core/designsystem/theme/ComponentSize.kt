package com.cocido.oficios.core.designsystem.theme

import androidx.compose.ui.unit.dp

object OficiosComponentSize {
    /** Altura de inputs — Figma h-48 px-16 py-12 r8 borde 1px */
    val textFieldHeight = 48.dp
    val minTouchTarget = 48.dp
    val buttonHeightLarge = 52.dp
    val buttonHeightMedium = 44.dp
    val buttonHeightSmall = 36.dp
    val buttonMinHeight = buttonHeightSmall
    /** Área táctil de íconos y controles que no son el CTA. */
    val buttonHeight = minTouchTarget
    val buttonPaddingHorizontal = 12.dp
    val buttonPaddingVertical = 8.dp
    val checkboxRowMinHeight = 48.dp
    val checkboxRowPaddingVertical = 12.dp
    val checkboxLabelGap = 12.dp
    val iconSm = 16.dp
    val iconMd = 24.dp
    val iconLg = 32.dp
    val checkboxSize = 18.dp
    val bottomBarHeight = 64.dp
    val topBarHeight = 56.dp
    val avatarSm = 48.dp
    val avatarMd = 64.dp
    val avatarLg = 96.dp
    val avatarSettings = 72.dp
    val ratingIcon = 36.dp
    val categoryCardSize = 112.dp
    /** Ancho de una card de carrusel, como fracción del viewport. */
    val carouselWidthFraction = 0.82f
    val agendaCell = 48.dp
    val listRowMinHeight = 56.dp
    val skeletonBlockMd = 160.dp
    val skeletonBlockLg = 240.dp
    val contentMaxWidth = 640.dp

    // Marca auth — baseline Figma 440dp (nodos 356:22, 356:49)
    val authBrandMaxWidth = 351.dp
    val authLogoWithTitleHeightCompact = 262.dp
    val authLogoWithTitleHeightSplash = 269.dp
    val authLogoIconWidthCompact = 122.dp
    val authLogoIconWidthSplash = 163.dp
    val authWordmarkHeight = 36.dp
    val authRegisterBrandIconWidth = 61.dp

    // Selección de rol — Figma 356:482
    val registerRoleCardHeight = 172.dp
    val registerRoleCardIconAreaHeight = 100.dp
    val registerRoleCardIconWorkerHeight = 64.dp
    val registerRoleCardIconClientHeight = 87.dp
}

// Alias de compatibilidad con código previo del template
@Deprecated("Usar OficiosComponentSize", replaceWith = ReplaceWith("OficiosComponentSize"))
object AppDimens {
    val iconSm = OficiosComponentSize.iconSm
    val iconMd = OficiosComponentSize.iconMd
    val iconLg = OficiosComponentSize.iconLg
    val buttonHeight = OficiosComponentSize.buttonHeight
    val textFieldHeight = OficiosComponentSize.textFieldHeight
    val topBarHeight = OficiosComponentSize.topBarHeight
    val contentMaxWidth = OficiosComponentSize.contentMaxWidth
}
