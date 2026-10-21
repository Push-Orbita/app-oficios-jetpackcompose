package com.cocido.oficios.core.designsystem.component.brand

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

enum class OficiosBrandScale {
    Compact,
    Splash,
}

/** Figma isotipo plano `666:2` (403×736). */
private const val LogoAspectHeightOverWidth = 736f / 403f

/**
 * Bloque de marca vertical: isotipo plano (casco azul + pin blanco) y
 * wordmark "Oficios Formosa". El lockup combinado vive en
 * `ic_oficios_logotipo_title` (Figma `583:39`) para [OficiosLogoVariant.WithTitle].
 */
@Composable
fun OficiosBrandBlock(
    modifier: Modifier = Modifier,
    scale: OficiosBrandScale = OficiosBrandScale.Compact,
) {
    val iconWidth = when (scale) {
        OficiosBrandScale.Compact -> OficiosComponentSize.authLogoIconWidthCompact
        OficiosBrandScale.Splash -> OficiosComponentSize.authLogoIconWidthSplash
    }
    val iconHeight: Dp = when (scale) {
        OficiosBrandScale.Compact -> OficiosComponentSize.authLogoIconHeightCompact
        OficiosBrandScale.Splash -> iconWidth * LogoAspectHeightOverWidth
    }
    val wordmarkHeight = when (scale) {
        OficiosBrandScale.Compact -> OficiosComponentSize.authWordmarkHeight
        OficiosBrandScale.Splash -> OficiosComponentSize.authWordmarkHeightSplash
    }
    val logoWordmarkGap = when (scale) {
        OficiosBrandScale.Compact -> OficiosComponentSize.authLoginLogoWordmarkGap
        OficiosBrandScale.Splash -> OficiosComponentSize.authSplashLogoWordmarkGap
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(logoWordmarkGap),
    ) {
        OficiosLogo(
            variant = OficiosLogoVariant.Icon,
            height = iconHeight,
            modifier = Modifier.width(iconWidth),
        )
        OficiosFormosaWordmark(height = wordmarkHeight)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B, name = "Compact")
@Composable
private fun OficiosBrandBlockCompactPreview() {
    OficiosTheme {
        OficiosBrandBlock(scale = OficiosBrandScale.Compact)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B, name = "Splash")
@Composable
private fun OficiosBrandBlockSplashPreview() {
    OficiosTheme {
        OficiosBrandBlock(scale = OficiosBrandScale.Splash)
    }
}
