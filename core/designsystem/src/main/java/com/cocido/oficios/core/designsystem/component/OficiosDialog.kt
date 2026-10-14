package com.cocido.oficios.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
// OficiosDialog — diálogo de confirmación / informativo
//
// Uso:
//   OficiosDialog(
//       title = "Eliminar pedido",
//       message = "Esta acción no se puede deshacer.",
//       confirmLabel = "Eliminar",
//       onConfirm = { ... },
//       onDismiss = { ... },
//   )
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficiosDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Cancelar",
    isDangerous: Boolean = false,
    actionSize: OficiosButtonSize = OficiosButtonSize.Large,
    confirmEnabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = com.cocido.oficios.core.designsystem.theme.OficiosElevation.none,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OficiosSpacing.lg),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(OficiosSpacing.sm))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (content != null) {
                    Spacer(Modifier.height(OficiosSpacing.sm))
                    content()
                }
                Spacer(Modifier.height(OficiosSpacing.lg))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    OficiosButton(
                        text = dismissLabel,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Ghost,
                        size = actionSize,
                    )
                    OficiosButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        enabled = confirmEnabled,
                        size = actionSize,
                        variant = if (isDangerous) OficiosButtonVariant.Danger else OficiosButtonVariant.Secondary,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Dialog Normal", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun DialogPreview() {
    OficiosTheme {
        OficiosDialog(
            title = "Confirmar acción",
            message = "¿Estás seguro de que querés continuar con esta operación?",
            confirmLabel = "Confirmar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Dialog Dangerous", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun DialogDangerousPreview() {
    OficiosTheme {
        OficiosDialog(
            title = "Eliminar pedido",
            message = "Esta acción no se puede deshacer. El pedido será eliminado permanentemente.",
            confirmLabel = "Eliminar",
            onConfirm = {},
            onDismiss = {},
            isDangerous = true,
        )
    }
}
