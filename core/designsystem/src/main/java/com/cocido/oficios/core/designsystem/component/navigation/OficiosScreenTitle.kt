package com.cocido.oficios.core.designsystem.component.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Título de pantalla estilo Figma — 36sp in-content con chevron izquierdo
 * 
 * Reemplaza OficiosTopAppBar/OficiosBackTopBar (M3 titleMedium 16sp) en todas las pantallas.
 * Figma: "‹ Título" DIN Bold 36 con chevron-left 24 y gap 10, más acción opcional a la derecha
 * (ej: campana 48dp con fondo #004DA8 r8).
 * 
 * @param title Título de la pantalla (ej: "Oficios", "Pedido Confirmado", "Recientes")
 * @param onNavigateBack Acción al tocar el chevron (null = sin chevron)
 * @param trailingAction Composable opcional a la derecha (ej: IconButton de notificaciones)
 */
@Composable
fun OficiosScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    trailingAction: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OficiosSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (onNavigateBack != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.ChevronLeft),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onNavigateBack),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge, // DIN Bold 36sp
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        
        if (trailingAction != null) {
            trailingAction()
        }
    }
}

@Preview(name = "Screen Title - Con navegación", showBackground = true)
@Composable
private fun PreviewOficiosScreenTitleWithBack() {
    OficiosTheme {
        OficiosScreenTitle(
            title = "Pedido Confirmado",
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Screen Title - Sin navegación", showBackground = true)
@Composable
private fun PreviewOficiosScreenTitleNoBack() {
    OficiosTheme {
        OficiosScreenTitle(
            title = "Inicio"
        )
    }
}

@Preview(name = "Screen Title - Con trailing action", showBackground = true)
@Composable
private fun PreviewOficiosScreenTitleWithAction() {
    OficiosTheme {
        OficiosScreenTitle(
            title = "Oficios",
            onNavigateBack = {},
            trailingAction = {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.BellDot),
                    contentDescription = "Notificaciones",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        )
    }
}
