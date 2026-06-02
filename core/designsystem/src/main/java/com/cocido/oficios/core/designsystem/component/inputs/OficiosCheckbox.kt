package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosCheckbox
// Basado en el diseño de Figma nodo 16:505 — checkbox magenta
// ---------------------------------------------------------------------------

@Composable
fun OficiosCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.size(OficiosComponentSize.checkboxSize),
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = OficiosAlpha.disabled),
                disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = OficiosAlpha.disabled),
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.disabled),
        )
    }
}

@Preview(name = "Checkbox States", showBackground = true, backgroundColor = 0xFF231D38)
@Composable
private fun CheckboxPreview() {
    OficiosTheme {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
            OficiosCheckbox(checked = false, onCheckedChange = {}, label = "Recordarme")
            OficiosCheckbox(checked = true, onCheckedChange = {}, label = "Acepto los términos y condiciones")
            OficiosCheckbox(checked = false, onCheckedChange = {}, label = "Deshabilitado", enabled = false)
        }
    }
}
