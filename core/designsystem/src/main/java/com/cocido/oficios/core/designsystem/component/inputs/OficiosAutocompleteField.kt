package com.cocido.oficios.core.designsystem.component.inputs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.feedback.OficiosProgress
import com.cocido.oficios.core.designsystem.theme.OficiosElevation
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

internal const val OFICIOS_AUTOCOMPLETE_MENU_TEST_TAG = "oficios_autocomplete_menu"

/**
 * Autocomplete con lista de sugerencias **inline** (en el árbol de composición),
 * para que uiautomator / TalkBack vean sugerencias, vacío y loading.
 * Evita confiar solo en [ExposedDropdownMenu], cuyo popup casi nunca entra al árbol semántico.
 */
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
    val loadingLabel = stringResource(R.string.ds_autocomplete_loading)
    val retryLabel = stringResource(R.string.ds_autocomplete_retry)
    val suggestionsListDescription = stringResource(R.string.ds_autocomplete_suggestions_list)
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

    Column(modifier = modifier.fillMaxWidth()) {
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
            modifier = Modifier.fillMaxWidth(),
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

        if (menuExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(OFICIOS_AUTOCOMPLETE_MENU_TEST_TAG)
                    .semantics { contentDescription = suggestionsListDescription },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = OficiosElevation.sm,
                shadowElevation = OficiosElevation.md,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when {
                        showError -> {
                            Text(
                                text = errorText.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = OficiosSpacing.md,
                                        vertical = OficiosSpacing.sm,
                                    )
                                    .semantics { contentDescription = errorText.orEmpty() },
                            )
                            if (onRetry != null) {
                                Text(
                                    text = retryLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onRetry.invoke() }
                                        .padding(
                                            horizontal = OficiosSpacing.md,
                                            vertical = OficiosSpacing.sm,
                                        )
                                        .semantics { contentDescription = retryLabel },
                                )
                            }
                        }
                        isLoading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = OficiosSpacing.md,
                                        vertical = OficiosSpacing.sm,
                                    )
                                    .semantics { contentDescription = loadingLabel },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                            ) {
                                OficiosProgress(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = loadingLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                        suggestions.isEmpty() -> {
                            Text(
                                text = resolvedNoResults,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = OficiosSpacing.md,
                                        vertical = OficiosSpacing.sm,
                                    )
                                    .semantics { contentDescription = resolvedNoResults },
                            )
                        }
                        else -> {
                            suggestions.forEachIndexed { index, option ->
                                val optionLabel = suggestionLabel(option)
                                Text(
                                    text = optionLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSuggestionSelected(option)
                                            userExpanded = false
                                            userDismissed = true
                                            searchSession = false
                                        }
                                        .padding(
                                            horizontal = OficiosSpacing.md,
                                            vertical = OficiosSpacing.sm,
                                        )
                                        .semantics { contentDescription = optionLabel },
                                )
                                if (index < suggestions.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
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
