package com.cocido.oficios.feature.auth.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandBlock
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandScale
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun AuthBrandHeader(
    modifier: Modifier = Modifier,
    scale: OficiosBrandScale = OficiosBrandScale.Compact,
) {
    OficiosBrandBlock(
        modifier = modifier,
        scale = scale,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun AuthBrandHeaderPreview() {
    OficiosTheme {
        AuthBrandHeader()
    }
}
