package com.cocido.oficios.core.designsystem.component.cards

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosSectionCard — card con título de sección opcional y acción
// ---------------------------------------------------------------------------

@Composable
fun OficiosSectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionContentDescription: String? = null,
    @DrawableRes leadingIcon: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        if (title != null || actionLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = OficiosSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            painter = painterResource(leadingIcon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (actionLabel != null && onAction != null) {
                    val description = actionContentDescription ?: actionLabel
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = description
                            }
                            .clickable(role = Role.Button, onClick = onAction)
                            .padding(start = OficiosSpacing.md, top = OficiosSpacing.xs, bottom = OficiosSpacing.xs),
                    )
                }
            }
        }
        content()
    }
}

@Preview(name = "Section Card", showBackground = true, backgroundColor = 0xFFF4F5F7)
@Composable
private fun SectionCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            OficiosSectionCard(title = "Beneficios", actionLabel = "Ver todos", onAction = {}) {
                Text("Contenido de la sección", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}