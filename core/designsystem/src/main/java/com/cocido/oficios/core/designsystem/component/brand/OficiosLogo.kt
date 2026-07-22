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
 * Variantes de marca:
 * - [WithTitle]: drawable combinado (`ic_oficios_logotipo_title`). Uso por defecto en login,
 *   splash y cualquier pantalla con logo + título apilados.
 * - [Icon]: solo el ícono (`ic_oficios_logotipo`). Usar junto a [OficiosFormosaWordmark] cuando
 *   el diseño ubica logo y título en fila horizontal.
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
    val drawable = when (variant) {
        OficiosLogoVariant.Icon -> R.drawable.ic_oficios_logotipo
        OficiosLogoVariant.WithTitle -> R.drawable.ic_oficios_logotipo_title
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
