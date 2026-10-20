package com.cocido.oficios.core.designsystem.component.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Wordmark separado (`oficios_formosa`). Usar solo en composición horizontal
 * junto a [OficiosLogo] con variante [OficiosLogoVariant.Icon] — ver [OficiosBrandRow].
 */
@Composable
fun OficiosFormosaWordmark(
    modifier: Modifier = Modifier,
    height: Dp = OficiosComponentSize.authWordmarkHeight,
    contentDescription: String? = "Oficios Formosa",
) {
    Image(
        painter = painterResource(R.drawable.oficios_formosa),
        contentDescription = contentDescription,
        modifier = modifier
            .widthIn(max = OficiosComponentSize.authBrandMaxWidth)
            .fillMaxWidth()
            .height(height),
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosFormosaWordmarkPreview() {
    OficiosTheme {
        OficiosFormosaWordmark()
    }
}
