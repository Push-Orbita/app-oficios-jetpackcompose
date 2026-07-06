package com.cocido.oficios.core.designsystem.component.brand

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

enum class OficiosBrandScale {
    Compact,
    Splash,
}

/**
 * Bloque de marca vertical (logo + título combinados).
 * Usa `ic_oficios_logotipo_title` — el caso habitual en auth y pantallas similares.
 *
 * Para logo a la izquierda y título a la derecha, usar [OficiosBrandRow].
 */
@Composable
fun OficiosBrandBlock(
    modifier: Modifier = Modifier,
    scale: OficiosBrandScale = OficiosBrandScale.Compact,
) {
    val height = when (scale) {
        OficiosBrandScale.Compact -> OficiosComponentSize.authLogoWithTitleHeightCompact
        OficiosBrandScale.Splash -> OficiosComponentSize.authLogoWithTitleHeightSplash
    }

    OficiosLogo(
        modifier = modifier,
        variant = OficiosLogoVariant.WithTitle,
        height = height,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF231D38, name = "Compact")
@Composable
private fun OficiosBrandBlockCompactPreview() {
    OficiosTheme {
        OficiosBrandBlock(scale = OficiosBrandScale.Compact)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF231D38, name = "Splash")
@Composable
private fun OficiosBrandBlockSplashPreview() {
    OficiosTheme {
        OficiosBrandBlock(scale = OficiosBrandScale.Splash)
    }
}
