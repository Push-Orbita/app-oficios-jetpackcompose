package com.cocido.oficios.core.designsystem.component.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Variantes de marca (Figma Thumbnail, casco azul + pin blanco):
 * - [WithTitle]: lockup plano `ic_oficios_logotipo_title` (nodo `583:39`).
 * - [Icon]: isotipo plano `ic_oficios_logotipo` (nodo `666:2`). Usar junto a
 *   [OficiosFormosaWordmark] en fila, o el outline `ic_oficios_logotipo_outline`
 *   (`736:40`) en launcher sobre fondo de marca.
 */
enum class OficiosLogoVariant {
    Icon,
    WithTitle,
}

@Composable
fun OficiosLogo(
    modifier: Modifier = Modifier,
    variant: OficiosLogoVariant = OficiosLogoVariant.WithTitle,
    height: Dp = when (variant) {
        OficiosLogoVariant.Icon -> 72.dp
        OficiosLogoVariant.WithTitle -> OficiosComponentSize.authLogoWithTitleHeightCompact
    },
    contentDescription: String? = "Oficios Formosa",
) {
    val drawable = when {
        variant == OficiosLogoVariant.Icon && OficiosTheme.isDark -> R.drawable.ic_oficios_logotipo
        variant == OficiosLogoVariant.Icon -> R.drawable.ic_oficios_logotipo_outline
        OficiosTheme.isDark -> R.drawable.ic_oficios_logotipo_title
        else -> R.drawable.ic_oficios_logotipo_title_outline
    }

    val logoModifier = when (variant) {
        OficiosLogoVariant.Icon -> modifier.height(height)
        OficiosLogoVariant.WithTitle -> modifier
            .widthIn(max = OficiosComponentSize.authBrandMaxWidth)
            .fillMaxWidth()
            .heightIn(max = height)
            .height(height)
    }

    Image(
        painter = painterResource(drawable),
        contentDescription = contentDescription,
        modifier = logoModifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B, name = "With title")
@Composable
private fun OficiosLogoWithTitlePreview() {
    OficiosTheme {
        OficiosLogo(variant = OficiosLogoVariant.WithTitle)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B, name = "Icon only")
@Composable
private fun OficiosLogoIconPreview() {
    OficiosTheme {
        OficiosLogo(variant = OficiosLogoVariant.Icon, height = 96.dp)
    }
}
