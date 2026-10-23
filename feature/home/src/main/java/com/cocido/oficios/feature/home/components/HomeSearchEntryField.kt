package com.cocido.oficios.feature.home.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.designsystem.component.inputs.OficiosSearchField
import com.cocido.oficios.core.ui.intent.dispatchSpokenSearch
import com.cocido.oficios.core.ui.intent.rememberVoiceSearchLauncher
import com.cocido.oficios.feature.home.R

@Composable
fun HomeSearchEntryField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null,
    filterContentDescription: String? = null,
    activeFilterCount: Int = 0,
    placeholderMaxLines: Int = 1,
    showMic: Boolean = true,
    onFieldClick: (() -> Unit)? = null,
) {
    val toastController = LocalOficiosToastController.current
    val unavailableMessage = stringResource(
        com.cocido.oficios.core.designsystem.R.string.ds_search_voice_unavailable,
    )
    val launchVoice = rememberVoiceSearchLauncher(
        prompt = placeholder,
        onSpokenQuery = { spoken ->
            dispatchSpokenSearch(
                spoken = spoken,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
            )
        },
        onUnavailable = { toastController.show(unavailableMessage) },
    )
    OficiosSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = placeholder,
        modifier = modifier,
        onMicClick = if (showMic) launchVoice else null,
        onSearchClick = { onSearch(query) },
        onFilterClick = onFilterClick,
        filterContentDescription = filterContentDescription,
        activeFilterCount = activeFilterCount,
        placeholderMaxLines = placeholderMaxLines,
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        onClick = onFieldClick,
    )
}

@Composable
fun HomeSearchEntryFieldWithDefaults(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeSearchEntryField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        placeholder = stringResource(R.string.home_search_hint),
        modifier = modifier,
    )
}
