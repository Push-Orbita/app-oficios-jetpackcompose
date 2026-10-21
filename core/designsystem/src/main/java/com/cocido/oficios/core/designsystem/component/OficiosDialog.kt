package com.cocido.oficios.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosElevation
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosDialog — diálogo de confirmación / informativo
//
// Acciones en fila (Cancelar Ghost a la izquierda, confirmar a la derecha),
// mismo ancho. Se apilan solo si el label no entra a fontScale alto.
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
    actionSize: OficiosButtonSize = OficiosButtonSize.Small,
    confirmEnabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = OficiosElevation.none,
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
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(OficiosSpacing.sm))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (content != null) {
                    Spacer(Modifier.height(OficiosSpacing.sm))
                    content()
                }
                Spacer(Modifier.height(OficiosSpacing.lg))
                OficiosDialogActions(
                    dismissLabel = dismissLabel,
                    onDismiss = onDismiss,
                    confirmLabel = confirmLabel,
                    onConfirm = onConfirm,
                    confirmEnabled = confirmEnabled,
                    isDangerous = isDangerous,
                    actionSize = actionSize,
                )
            }
        }
    }
}

/**
 * Acciones de diálogo: fila de igual ancho (weight 1, gap 12 dp).
 * Se apilan a ancho completo solo cuando un label no entra a fontScale alto.
 */
@Composable
fun OficiosDialogActions(
    dismissLabel: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    isDangerous: Boolean = false,
    actionSize: OficiosButtonSize = OficiosButtonSize.Small,
) {
    val confirmVariant = if (isDangerous) {
        OficiosButtonVariant.Danger
    } else {
        OficiosButtonVariant.Secondary
    }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stackVertically = dialogActionsShouldStack(
            dismissLabel = dismissLabel,
            confirmLabel = confirmLabel,
            availableWidth = maxWidth,
        )
        if (stackVertically) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                OficiosButton(
                    text = dismissLabel,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Ghost,
                    size = actionSize,
                )
                OficiosButton(
                    text = confirmLabel,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = confirmEnabled,
                    size = actionSize,
                    variant = confirmVariant,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
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
                    variant = confirmVariant,
                )
            }
        }
    }
}

@Composable
private fun dialogActionsShouldStack(
    dismissLabel: String,
    confirmLabel: String,
    availableWidth: Dp,
): Boolean {
    if (!availableWidth.isFinite() || availableWidth <= 0.dp) return false
    val gap = OficiosSpacing.sm
    val horizontalPad = OficiosComponentSize.buttonPaddingHorizontal * 2
    val slotWidth = (availableWidth - gap) / 2
    val maxTextWidth = slotWidth - horizontalPad
    if (maxTextWidth <= 0.dp) return true
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelLarge
    val density = LocalDensity.current
    fun labelFits(label: String): Boolean {
        val widthPx = textMeasurer.measure(
            text = label,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        ).size.width
        return with(density) { widthPx.toDp() } <= maxTextWidth
    }
    return !labelFits(dismissLabel) || !labelFits(confirmLabel)
}

private fun Dp.isFinite(): Boolean = value.isFinite()

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Dialog Normal", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun DialogPreview() {
    OficiosTheme {
        OficiosDialog(
            title = "Confirmar acción",
            message = "¿Está seguro de que quiere continuar con esta operación?",
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
            title = "Eliminar foto",
            message = "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = {},
            onDismiss = {},
            isDangerous = true,
        )
    }
}

@Preview(
    name = "Dialog Dangerous fontScale 2",
    showBackground = true,
    backgroundColor = 0xFF00358B,
    fontScale = 2f,
)
@Composable
private fun DialogDangerousLargeFontPreview() {
    OficiosTheme {
        OficiosDialog(
            title = "Eliminar foto",
            message = "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar permanentemente",
            dismissLabel = "Cancelar y volver",
            onConfirm = {},
            onDismiss = {},
            isDangerous = true,
        )
    }
}
