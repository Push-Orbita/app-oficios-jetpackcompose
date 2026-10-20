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
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

enum class OficiosBrandScale {
    Compact,
    Splash,
}

private const val LogoAspectHeightOverWidth = 270f / 165f

/**
 * Bloque de marca vertical: ícono (casco) + wordmark "Oficios Formosa".
 * Se componen por separado para que el casco no quede achicado por el
 * wordmark ancho del drawable combinado `ic_oficios_logotipo_title`.
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
    val iconHeight: Dp = iconWidth * LogoAspectHeightOverWidth
    val wordmarkHeight = when (scale) {
        OficiosBrandScale.Compact -> OficiosComponentSize.authWordmarkHeight
        OficiosBrandScale.Splash -> OficiosComponentSize.authWordmarkHeightSplash
    }
    val logoWordmarkGap = when (scale) {
        OficiosBrandScale.Compact -> OficiosSpacing.md
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
