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
    val categoryCardSize = 118.dp
    /** Figma 224:1013 — Recientes: título + gap 10 + cards 118. */
    val homeRecentsSectionMinHeight = 184.dp
    /** Compensa el capital-top de D-DIN vs DIN Pro (título rel 155). */
    val homeRecentsAfterSearchExtra = 6.dp
    /** Gap título Recientes → cards para dejar las cards en rel 199. */
    val homeRecentsTitleToCards = 13.dp
    /** Caps del label 20dp bajo el top de la card (includeFontPadding ≈ 6). */
    val homeRecentCardLabelTop = 14.dp
    /** Espacio óptico recents→soporte para dejar la card en y≈373. */
    val homeSupportAfterRecentsExtra = 23.dp
    /** Resultados 40:322: fila logo+título. 107 de Figma + 5 para el buscador en y=195. */
    val searchResultsHeaderMinHeight = 112.dp
    /** Gap buscador → primera WorkerCard (243 → 259). */
    val searchResultsListTop = 16.dp
    val searchResultsLogoOffsetX = 3.dp
    val searchResultsLogoOffsetY = (-3).dp
    val workerCardMinHeight = 184.dp
    val workerCardTextTopInset = 4.dp
    val workerCardNameToRating = 7.dp
    val workerCardHeaderToSpecialty = 11.dp
    val workerCardSpecialtyToActions = 18.dp
    /** Ficha 40:559: slide de galería 376×376 (440 − 32 − 32). */
    val workerGallerySize = 376.dp
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
    val authLoginContentTopPadding = 63.dp
    /** Login error: «!» 22dp, centrado con la línea de texto. */
    val authLoginErrorIconSize = 22.dp
    /** Login error: gap mensaje→campo. 15dp deja el bloque de error en ±2.3 de Figma. */
    val authLoginErrorToField = 15.dp
    /** Login: wordmark → subtítulo. +6 vs r6 para bajar el bloque a y_rel 377. */
    val authLoginBrandToSubtitle = 64.dp
    /** Forgot password reutilizaba el gap de login; se queda en el valor previo. */
    val authForgotBrandToSubtitle = 58.dp
    val authLoginSubtitleToField = 18.dp
    val authLoginPasswordToActions = 16.dp
    val authLoginActionsToDivider = 16.dp
    val authLoginDividerToPrompt = 16.dp
    /** Login: prompt → soporte. −6 vs r6 para dejar la card en 694 / 698. */
    val authLoginPromptToSupport = 49.dp
    val authLoginSupportToForgot = 16.dp
    val authLoginSupportCardMinHeight = 112.dp
    val authRegisterHeaderWidth = 345.dp
    val authRegisterBrandIconWidth = 62.dp
    val authRegisterBrandIconHeight = 102.dp
    val authRegisterBrandSpacing = 24.dp
    /** Wordmark del header de rol: ~7% más chico, glifo x118–377. */
    val authRegisterHeaderWordmarkHeight = 27.dp
    val authRegisterHeaderWordmarkWidth = 259.dp
    /** Rol 356:482: status 48 + 18 = logo glifo y=67. */
    val authRegisterHeaderTop = 18.dp
    /** Registro: status 48 + 22 = mayúsculas en y=75 (glifo hug, no caja D-DIN). */
    val authRegisterTitleTop = 22.dp
    /** Figma: subtítulo 21dp bajo el baseline del título. 22 + glifo hug ≈ 21. */
    val authRegisterTitleToSubtitle = 22.dp
    val authRegisterSubtitleToField = 19.dp
    val authRegisterEmailToHint = 19.dp
    val authRegisterHintToPassword = 18.dp
    val authRegisterPasswordToTerms = 17.dp
    val authRegisterTermsToDivider = 16.dp
    val authRegisterDividerToPrompt = 16.dp
    val authRegisterTermsCheckboxToText = 8.dp
    val authSupportBadgeWidth = 31.dp
    val authSupportBadgeHeight = 36.dp
    val authSupportBadgeSize = authSupportBadgeWidth
    val authSupportBadgeOffsetY = 18.dp
    val authSupportBadgeOffsetX = 15.dp
    /** Login/rol soporte: Figma 20dp arriba y 20dp abajo; el texto se centra. */
    val authSupportCardPaddingTop = 20.dp
    val authSupportCardPaddingBottom = 20.dp
    val authRoleHeaderToSubtitle = 21.dp
    val authRoleSubtitleToCards = 18.dp
    val authRoleCardsToDivider = 16.dp
    val authRoleDividerToPrompt = 16.dp
    val authRolePromptToSupport = 52.dp
    val authRoleSupportCardMinHeight = 92.dp
    /** Rol: título ~20dp bajo el top de la card (caja 18 + glifo). */
    val authRoleCardTitleTopInset = 18.dp
    /** Casco: top visual ~61dp bajo el top de la card. */
    val authRoleCardIconTopInsetWorker = 16.dp
    /** Lupa: top visual ~52dp bajo el top de la card. */
    val authRoleCardIconTopInsetClient = 18.dp

    // Selección de rol — Figma 356:482 / QA r6 (alto mid-edge 172)
    val registerRoleCardWidth = 179.dp
    /** Home/grids: no subir. El alto de las cards de Rol es [authRoleCardMinHeight]. */
    val registerRoleCardHeight = 160.dp
    val authRoleCardMinHeight = 172.dp
    val registerRoleCardGap = 16.dp
    val registerRoleCardIconAreaHeight = 100.dp
    /** Home/grids: no subir. El casco de Rol auth es [authRoleCardIconWorkerHeight]. */
    val registerRoleCardIconWorkerHeight = 84.dp
    /** Rol auth: ic_casco nativo 116×82 (visible ~104.9×72.1). Sin rotación extra. */
    val authRoleCardIconWorkerWidth = 116.dp
    val authRoleCardIconWorkerHeight = 82.dp
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
