package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosElevation
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficiosDropdownField(
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { requested ->
            if (enabled) expanded = requested && options.isNotEmpty()
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        OficiosTextField(
            value = value,
            onValueChange = {},
            label = label,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            placeholder = placeholder,
            enabled = enabled,
            readOnly = true,
            isError = isError,
            supportingText = supportingText,
            trailingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (expanded) OficiosIcons.ChevronDown else OficiosIcons.ChevronDown
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        ExposedDropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = OficiosElevation.sm,
            shadowElevation = OficiosElevation.md,
            modifier = Modifier.heightIn(max = 280.dp),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(name = "Dropdown Field", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun DropdownFieldPreview() {
    OficiosTheme {
        Column {
            OficiosDropdownField(
                value = "Construcción",
                options = listOf("Construcción", "Mantenimiento", "Bienestar"),
                onOptionSelected = {},
                label = "Rubro",
                placeholder = "Seleccione un rubro",
            )
        }
    }
}
