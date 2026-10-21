package com.cocido.oficios.feature.home.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosDialogActions
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.chips.OficiosFilterChip
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.AttentionModality
import com.cocido.oficios.feature.home.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBottomSheet(
    state: FilterSheetUiState,
    onEvent: (SearchResultsEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onEvent(SearchResultsEvent.DismissFilters) },
        sheetState = sheetState,
        containerColor = OficiosColorBackground,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OficiosSpacing.lg)
                .padding(bottom = OficiosSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.home_search_filters),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (state.categories.isNotEmpty()) {
                OficiosDropdownField(
                    label = stringResource(R.string.home_filter_category),
                    value = state.selectedCategoryLabel.orEmpty(),
                    options = state.categories.map { it.label },
                    onOptionSelected = { selected ->
                        state.categories.firstOrNull { it.label == selected }?.let {
                            onEvent(SearchResultsEvent.FilterCategorySelected(it))
                        }
                    },
                    placeholder = stringResource(R.string.home_filter_category_hint),
                )
            }

            if (state.rubros.isNotEmpty()) {
                OficiosDropdownField(
                    label = stringResource(R.string.home_filter_rubro),
                    value = state.selectedRubroLabel.orEmpty(),
                    options = state.rubros.map { it.label },
                    onOptionSelected = { selected ->
                        state.rubros.firstOrNull { it.label == selected }?.let {
                            onEvent(SearchResultsEvent.FilterRubroSelected(it))
                        }
                    },
                    placeholder = stringResource(R.string.home_filter_rubro_hint),
                )
            }

            if (state.subRubros.isNotEmpty()) {
                OficiosDropdownField(
                    label = stringResource(R.string.home_filter_subrubro),
                    value = state.selectedSubRubroLabel.orEmpty(),
                    options = state.subRubros.map { it.label },
                    onOptionSelected = { selected ->
                        state.subRubros.firstOrNull { it.label == selected }?.let {
                            onEvent(SearchResultsEvent.FilterSubRubroSelected(it))
                        }
                    },
                    placeholder = stringResource(R.string.home_filter_subrubro_hint),
                )
            }

            OficiosAutocompleteField(
                value = state.barrioQuery,
                onValueChange = { onEvent(SearchResultsEvent.FilterBarrioQueryChanged(it)) },
                suggestions = state.barrioSuggestions,
                onSuggestionSelected = { onEvent(SearchResultsEvent.FilterBarrioSelected(it)) },
                suggestionLabel = { it.label },
                label = stringResource(R.string.home_filter_zone),
                placeholder = stringResource(R.string.home_filter_zone_hint),
                isLoading = state.isSearchingBarrios,
                onFocused = { onEvent(SearchResultsEvent.FilterBarrioFocused) },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                ModalityFilterChip(
                    modality = AttentionModality.DOMICILIO,
                    label = stringResource(R.string.home_filter_modality_home),
                    selectedModality = state.selectedModality,
                    onEvent = onEvent,
                )
                ModalityFilterChip(
                    modality = AttentionModality.LOCAL,
                    label = stringResource(R.string.home_filter_modality_local),
                    selectedModality = state.selectedModality,
                    onEvent = onEvent,
                )
            }

            OficiosButton(
                text = state.selectedDate ?: stringResource(R.string.home_filter_date),
                onClick = { showDatePicker = true },
                variant = OficiosButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                OficiosButton(
                    text = stringResource(R.string.home_filter_clear),
                    onClick = { onEvent(SearchResultsEvent.ClearFilters) },
                    variant = OficiosButtonVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
                OficiosButton(
                    text = stringResource(R.string.home_filter_apply),
                    onClick = { onEvent(SearchResultsEvent.ApplyFilters) },
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Large,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                OficiosDialogActions(
                    dismissLabel = stringResource(R.string.home_back),
                    onDismiss = { showDatePicker = false },
                    confirmLabel = stringResource(R.string.home_filter_apply),
                    onConfirm = {
                        val millis = datePickerState.selectedDateMillis
                        val formatted = millis?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                        }
                        onEvent(SearchResultsEvent.FilterDateChanged(formatted))
                        showDatePicker = false
                    },
                )
            },
            dismissButton = {},
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ModalityFilterChip(
    modality: AttentionModality,
    label: String,
    selectedModality: AttentionModality?,
    onEvent: (SearchResultsEvent) -> Unit,
) {
    val selected = selectedModality == modality
    OficiosFilterChip(
        label = label,
        selected = selected,
        onToggle = {
            onEvent(SearchResultsEvent.FilterModalitySelected(if (selected) null else modality))
        },
    )
}
