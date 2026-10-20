package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// CategoryCard — tarjeta de rubro/categoría de servicio según Figma
// Grilla 2 col con label arriba e ilustración ~100px
// ---------------------------------------------------------------------------

@Composable
fun CategoryCard(
    label: String,
    iconRes: Int, // DrawableRes del ícono/ilustración
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    OficiosCard(
        onClick = onClick,
        selected = isSelected,
        modifier = modifier,
        contentPadding = PaddingValues(OficiosSpacing.md),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconRes),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(64.dp), // ~100px en Figma, 64dp es razonable
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
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
            CategoryCard(label = "Construcción", iconRes = OficiosIcons.Hammer, onClick = {})
            CategoryCard(label = "Seguridad", iconRes = OficiosIcons.ShieldCheck, onClick = {}, isSelected = true)
        }
    }
}
