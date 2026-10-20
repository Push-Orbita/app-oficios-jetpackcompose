package com.cocido.oficios.core.designsystem.component.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun OficiosIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    iconSize: Dp = OficiosComponentSize.iconMd,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(OficiosSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Preview(name = "Icon Button", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun IconButtonPreview() {
    OficiosTheme {
        OficiosIconButton(
            icon = ImageVector.vectorResource(OficiosIcons.BellDot),
            contentDescription = "Notificaciones",
            onClick = {},
        )
    }
}
