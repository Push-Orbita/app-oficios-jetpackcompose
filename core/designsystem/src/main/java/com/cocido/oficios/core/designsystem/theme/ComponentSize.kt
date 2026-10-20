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

    // Marca auth — baseline Figma 440dp (nodos 16:2, 356:49, 356:482)
    val authBrandMaxWidth = 351.dp
    val authLogoWithTitleHeightCompact = 262.dp
    val authLogoWithTitleHeightSplash = 269.dp
    val authLogoIconWidthCompact = 122.dp
    /** Splash 16:2: casco+pin ~164×268dp. */
    val authLogoIconWidthSplash = 164.dp
    /**
     * Gap layout logo→wordmark. El drawable aporta ~6dp de aire; 23dp deja
     * el wordmark visual en y≈628 (gap óptico 29) con logo top 330.
     */
    val authSplashLogoWordmarkGap = 23.dp
    val authSplashLogoTop = 330.dp
    val authWordmarkHeight = 36.dp
    val authWordmarkHeightSplash = 48.dp
    /** Login 356:49: logo 122×201.75. */
    val authLogoIconHeightCompact = 202.dp
    /** Login 356:49: gap logo→wordmark 13dp (wordmark visual y=327). */
    val authLoginLogoWordmarkGap = 13.dp
    /** Login 356:49: status 48 + 64 = logo y=112. */
    val authLoginContentTopPadding = 64.dp
    val authLoginBrandToSubtitle = 58.dp
    val authLoginSubtitleToField = 18.dp
    val authLoginPasswordToActions = 16.dp
    val authLoginActionsToDivider = 16.dp
    val authLoginDividerToPrompt = 16.dp
    /** Login: prompt bottom 693 → soporte 746 (error 750). */
    val authLoginPromptToSupport = 53.dp
    val authLoginSupportToForgot = 16.dp
    val authLoginSupportCardHeight = 101.dp
    val authRegisterHeaderWidth = 345.dp
    val authRegisterBrandIconWidth = 62.dp
    val authRegisterBrandIconHeight = 102.dp
    val authRegisterBrandSpacing = 24.dp
    /** Rol 356:482: status 48 + 19 = logo y=67. */
    val authRegisterHeaderTop = 19.dp
    /** Registro: título y=75 (status 48 + 27). */
    val authRegisterTitleTop = 27.dp
    val authRegisterTitleToSubtitle = 11.dp
    val authRegisterSubtitleToField = 13.dp
    val authRegisterEmailToHint = 19.dp
    val authRegisterHintToPassword = 13.dp
    val authRegisterPasswordToTerms = 17.dp
    val authRegisterTermsToDivider = 13.dp
    val authRegisterDividerToPrompt = 16.dp
    val authRegisterTermsCheckboxToText = 8.dp
    val authSupportBadgeSize = 32.dp
    val authSupportBadgeOffsetY = 10.dp
    val authRoleHeaderToSubtitle = 16.dp
    val authRoleSubtitleToCards = 23.dp
    val authRoleCardsToDivider = 22.dp
    val authRoleDividerToPrompt = 16.dp
    val authRolePromptToSupport = 59.dp
    val authRoleSupportCardHeight = 80.dp
    val authRoleCardIconTopInset = 12.dp

    // Selección de rol — Figma 356:482 / QA r3
    val registerRoleCardWidth = 179.dp
    val registerRoleCardHeight = 160.dp
    val registerRoleCardGap = 16.dp
    val registerRoleCardIconAreaHeight = 100.dp
    val registerRoleCardIconWorkerHeight = 84.dp
    val registerRoleCardIconClientHeight = 84.dp
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
