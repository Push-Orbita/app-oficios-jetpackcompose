package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// CategoryCard — tarjeta de rubro/categoría de servicio
// ---------------------------------------------------------------------------

@Composable
fun CategoryCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    OficiosCard(
        onClick = onClick,
        selected = isSelected,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(OficiosSpacing.md),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(name = "Category Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun CategoryCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            CategoryCard(label = "Construcción", icon = Icons.Outlined.Construction, onClick = {})
            CategoryCard(label = "Seleccionado", icon = Icons.Outlined.Construction, onClick = {}, isSelected = true)
        }
    }
}
