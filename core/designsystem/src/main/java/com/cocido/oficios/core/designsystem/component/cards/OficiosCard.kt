package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosCard — componente base
//
// Figma: bg #231D38, border #413764 (1dp), radius 16dp, padding 16dp
//
// Uso:
//   OficiosCard { /* contenido */ }
//   OficiosCard(onClick = { ... }) { /* contenido clicable */ }
//   OficiosCard(selected = true) { /* seleccionado → borde primary */ }
// ---------------------------------------------------------------------------

@Composable
fun OficiosCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(OficiosSpacing.md),
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit,
) {
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        disabledContainerColor = containerColor.copy(alpha = 0.6f),
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = MaterialTheme.shapes.large,
            colors = cardColors,
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = cardColors,
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Preview(name = "Oficios Card", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun OficiosCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            OficiosCard {
                Text("Card base", style = MaterialTheme.typography.bodyLarge)
            }
            OficiosCard(selected = true, modifier = Modifier.padding(top = OficiosSpacing.md)) {
                Text("Card seleccionada", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
