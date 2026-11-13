package com.cocido.oficios.core.designsystem.component.inputs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import com.cocido.oficios.core.designsystem.theme.OficiosElevation
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

internal const val OFICIOS_AUTOCOMPLETE_MENU_TEST_TAG = "oficios_autocomplete_menu"

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
    isLoading: Boolean = false,
    noResultsText: String? = null,
    errorText: String? = null,
    onRetry: (() -> Unit)? = null,
    onFocused: () -> Unit = {},
    onSuggestionsDismissed: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var userExpanded by remember { mutableStateOf(false) }
    var searchSession by remember { mutableStateOf(false) }
    var userDismissed by remember { mutableStateOf(false) }
    var isFieldFocused by remember { mutableStateOf(false) }
    val resolvedNoResults = noResultsText ?: stringResource(R.string.ds_autocomplete_no_results)
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var wasImeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading, suggestions, errorText) {
        if (isLoading) searchSession = true
        if (!userDismissed && (isLoading || suggestions.isNotEmpty() || errorText != null)) {
            userExpanded = true
        }
    }

    val showError = !isLoading && errorText != null && userExpanded
    val showNoResults = searchSession &&
        !isLoading &&
        suggestions.isEmpty() &&
        errorText == null &&
        userExpanded
    val hasMenuContent = isLoading || suggestions.isNotEmpty() || showNoResults || showError
    val menuExpanded = userExpanded && hasMenuContent && enabled

    fun dismissMenu() {
        userExpanded = false
        userDismissed = true
        searchSession = false
        onSuggestionsDismissed()
    }

    fun dismissKeyboardAndSuggestions() {
        keyboardController?.hide()
        dismissMenu()
    }

    LaunchedEffect(imeVisible) {
        val closedIme = wasImeVisible && !imeVisible
        wasImeVisible = imeVisible
        if (closedIme && userExpanded) {
            dismissMenu()
        }
    }

    BackHandler(enabled = menuExpanded) {
        dismissKeyboardAndSuggestions()
    }

    ExposedDropdownMenuBox(
        expanded = menuExpanded,
        onExpandedChange = { requested ->
            if (requested) {
                userDismissed = false
                userExpanded = true
                // Only notify once per focus session. Material3 may re-emit
                // onExpandedChange(true) when isLoading/suggestions flip and
                // LaunchedEffect sets userExpanded — that re-entry used to
                // retrigger barrio search in a tight loop (ANR).
                if (!isFieldFocused) {
                    isFieldFocused = true
                    onFocused()
                }
            } else {
                isFieldFocused = false
                dismissKeyboardAndSuggestions()
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        OficiosTextField(
            value = value,
            onValueChange = {
                userDismissed = false
                onValueChange(it)
            },
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
            onFocusChange = { focused ->
                if (focused) {
                    userDismissed = false
                    userExpanded = true
                    if (!isFieldFocused) {
                        isFieldFocused = true
                        onFocused()
                    }
                } else {
                    isFieldFocused = false
                }
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        ExposedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { dismissKeyboardAndSuggestions() },
            modifier = Modifier
                .testTag(OFICIOS_AUTOCOMPLETE_MENU_TEST_TAG)
                .heightIn(max = 240.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = OficiosElevation.sm,
            shadowElevation = OficiosElevation.md,
        ) {
            when {
                showError -> {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = errorText.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {},
                        enabled = false,
                        contentPadding = PaddingValues(
                            horizontal = OficiosSpacing.md,
                            vertical = OficiosSpacing.xs,
                        ),
                    )
                    if (onRetry != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.ds_autocomplete_retry),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            onClick = { onRetry.invoke() },
                            contentPadding = PaddingValues(
                                horizontal = OficiosSpacing.md,
                                vertical = OficiosSpacing.xs,
                            ),
                        )
                    }
                }
                isLoading -> {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                            ) {
                                OficiosProgress(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(R.string.ds_autocomplete_loading),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        },
                        onClick = {},
                        enabled = false,
                        contentPadding = PaddingValues(
                            horizontal = OficiosSpacing.md,
                            vertical = OficiosSpacing.xs,
                        ),
                    )
                }
                suggestions.isEmpty() -> {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = resolvedNoResults,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = {},
                        enabled = false,
                        contentPadding = PaddingValues(
                            horizontal = OficiosSpacing.md,
                            vertical = OficiosSpacing.xs,
                        ),
                    )
                }
                else -> {
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
                                userExpanded = false
                                userDismissed = true
                                searchSession = false
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
            placeholder = "Busque y seleccione el barrio",
        )
    }
}
