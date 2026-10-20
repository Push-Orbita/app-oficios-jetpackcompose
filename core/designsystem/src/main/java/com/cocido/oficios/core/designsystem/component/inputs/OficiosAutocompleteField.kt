package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OficiosAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<T>,
    onSuggestionSelected: (T) -> Unit,
    suggestionLabel: (T) -> String,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    onSuggestionsDismissed: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(suggestions) {
        expanded = suggestions.isNotEmpty()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { isExpanded ->
            expanded = isExpanded
            if (!isExpanded) onSuggestionsDismissed()
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        OficiosTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            enabled = enabled,
            isError = isError,
            supportingText = supportingText,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onSuggestionsDismissed()
            },
            modifier = Modifier.heightIn(max = 240.dp),
        ) {
            suggestions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = suggestionLabel(option),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = {
                        onSuggestionSelected(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(
                        horizontal = OficiosSpacing.md,
                        vertical = OficiosSpacing.xs,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Autocomplete Field", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun AutocompleteFieldPreview() {
    OficiosTheme {
        OficiosAutocompleteField(
            value = "San",
            onValueChange = {},
            suggestions = listOf("San Francisco", "San Martín", "San Isidro"),
            onSuggestionSelected = {},
            suggestionLabel = { it },
            label = "Barrio",
            placeholder = "Buscá tu barrio",
        )
    }
}
