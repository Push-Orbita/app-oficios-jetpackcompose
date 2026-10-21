package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.border.dashedBorder
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Tile "Añadir" con borde punteado — Figma Editar Perfil `231:15`.
 */
@Composable
fun DashedAddTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(OficiosRadius.lg)
    Box(
        modifier = modifier
            .size(OficiosComponentSize.profileMediaTile)
            .clip(shape)
            .dashedBorder(OficiosColorOutlineFill, OficiosRadius.lg)
            .clickable(onClick = onClick)
            .padding(OficiosSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorWhite,
            )
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.CirclePlus),
                contentDescription = null,
                tint = OficiosColorWhite,
                modifier = Modifier.size(OficiosComponentSize.iconLg),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewDashedAddTile() {
    OficiosTheme {
        Box(modifier = Modifier.fillMaxSize().padding(OficiosSpacing.md)) {
            DashedAddTile(label = "Añadir", onClick = {})
        }
    }
}
