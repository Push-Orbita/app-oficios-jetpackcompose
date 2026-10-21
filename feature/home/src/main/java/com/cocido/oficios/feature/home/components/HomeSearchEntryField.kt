package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.inputs.OficiosSearchField
import com.cocido.oficios.feature.home.R

@Composable
fun HomeSearchEntryField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onMicClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    filterContentDescription: String? = null,
    activeFilterCount: Int = 0,
    placeholderMaxLines: Int = 1,
    showMic: Boolean = true,
) {
    OficiosSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = placeholder,
        modifier = modifier,
        onMicClick = if (showMic) onMicClick ?: {} else null,
        onSearchClick = onSearch,
        onFilterClick = onFilterClick,
        filterContentDescription = filterContentDescription,
        activeFilterCount = activeFilterCount,
        placeholderMaxLines = placeholderMaxLines,
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@Composable
fun HomeSearchEntryFieldWithDefaults(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onMicClick: (() -> Unit)? = null,
) {
    HomeSearchEntryField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        placeholder = stringResource(R.string.home_search_hint),
        modifier = modifier,
        onMicClick = onMicClick,
    )
}
