package com.cocido.oficios.core.designsystem.component.brand

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Marca en fila horizontal: ícono a la izquierda y wordmark a la derecha.
 * Solo para diseños que lo indiquen explícitamente (p. ej. header de home).
 */
@Composable
fun OficiosBrandRow(
    modifier: Modifier = Modifier,
    iconWidth: Dp = OficiosComponentSize.authLogoIconWidthCompact,
    wordmarkHeight: Dp = OficiosComponentSize.authWordmarkHeight,
    spacing: Dp = OficiosSpacing.sm,
) {
    val iconHeight = iconWidth * (270f / 165f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OficiosLogo(
            variant = OficiosLogoVariant.Icon,
            height = iconHeight,
            modifier = Modifier.width(iconWidth),
        )
        OficiosFormosaWordmark(
            modifier = Modifier.height(wordmarkHeight),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosBrandRowPreview() {
    OficiosTheme {
        OficiosBrandRow()
    }
}
