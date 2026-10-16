package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun OficiosCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    expandWidth: Boolean = true,
    compactRow: Boolean = false,
) {
    val rowModifier = modifier
        .then(if (expandWidth) Modifier.fillMaxWidth() else Modifier)
        .then(
            if (compactRow) {
                Modifier
                    .heightIn(min = OficiosComponentSize.checkboxRowMinHeight)
                    .padding(start = OficiosSpacing.xxs)
            } else {
                Modifier
            },
        )
        .semantics(mergeDescendants = true) { contentDescription = label }
        .clickable(enabled = enabled) { onCheckedChange(!checked) }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosComponentSize.checkboxLabelGap),
    ) {
        Box(
            modifier = if (compactRow) {
                Modifier.padding(
                    top = OficiosComponentSize.checkboxRowPaddingVertical,
                    bottom = OficiosComponentSize.checkboxRowPaddingVertical,
                )
            } else {
                Modifier
            },
            contentAlignment = Alignment.Center,
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
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.disabled)
            },
        )
    }
}

@Preview(name = "Checkbox States", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun CheckboxPreview() {
    OficiosTheme {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.none)) {
            OficiosCheckbox(
                checked = true,
                onCheckedChange = {},
                label = "Especialización 1",
                compactRow = true,
            )
            OficiosCheckbox(
                checked = false,
                onCheckedChange = {},
                label = "Especialización 2",
                compactRow = true,
            )
        }
    }
}
