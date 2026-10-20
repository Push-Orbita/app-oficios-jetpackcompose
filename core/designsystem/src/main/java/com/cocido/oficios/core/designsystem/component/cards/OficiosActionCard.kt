package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosActionCard — card con título, descripción, acción primaria y
//                      una acción secundaria opcionales
// ---------------------------------------------------------------------------

@Composable
fun OficiosActionCard(
    title: String,
    description: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OficiosButton(
                text = primaryActionLabel,
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Medium,
            )
            if (secondaryActionLabel != null && onSecondaryAction != null) {
                OficiosButton(
                    text = secondaryActionLabel,
                    onClick = onSecondaryAction,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Medium,
                )
            }
        }
    }
}

@Preview(name = "Action Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ActionCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            OficiosActionCard(
                title = "Completá tu perfil",
                description = "Para acceder a todas las funciones necesitás completar tu información.",
                primaryActionLabel = "Completar perfil",
                onPrimaryAction = {},
                secondaryActionLabel = "Más tarde",
                onSecondaryAction = {},
            )
        }
    }
}
