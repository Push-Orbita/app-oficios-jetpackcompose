package com.cocido.oficios.feature.settings.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosAddressLocationPicker
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler
import com.cocido.oficios.feature.settings.R

@Composable
fun CommercialLocalScreen(
    uiState: CommercialLocalUiState,
    onEvent: (CommercialLocalEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = uiState.hasUnsavedChanges,
        onConfirmExit = { onEvent(CommercialLocalEvent.Back) },
    )
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.commercial_local_title),
                onBack = handleBack,
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.commercial_local_load_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(CommercialLocalEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty -> OficiosEmptyState(
                    title = stringResource(R.string.commercial_local_missing_service_title),
                    description = stringResource(R.string.commercial_local_missing_service_description),
                    ctaLabel = stringResource(R.string.commercial_local_add_service),
                    onCta = { onEvent(CommercialLocalEvent.AddService) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Content -> CommercialLocalForm(state, onEvent)
            }
        }
    }
}

private fun CommercialLocalUiState.phase(): OficiosContentPhase = when {
    isLoading -> OficiosContentPhase.Loading
    missingService -> OficiosContentPhase.Empty
    loadFailed -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun CommercialLocalForm(
    state: CommercialLocalUiState,
    onEvent: (CommercialLocalEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md)
            .widthIn(max = OficiosComponentSize.contentMaxWidth),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.errorMessage != null) {
            OficiosInlineAlert(message = state.errorMessage, modifier = Modifier.fillMaxWidth())
        }
        OficiosAutocompleteField(
            value = state.barrioQuery,
            onValueChange = { onEvent(CommercialLocalEvent.BarrioQueryChanged(it)) },
            suggestions = state.barrioOptions,
            onSuggestionSelected = { onEvent(CommercialLocalEvent.BarrioSelected(it)) },
            suggestionLabel = { it.label },
            label = stringResource(R.string.services_neighborhood),
            placeholder = stringResource(R.string.services_neighborhood_hint),
            isLoading = state.isSearchingBarrios,
            onFocused = { onEvent(CommercialLocalEvent.BarrioFocused) },
            onSuggestionsDismissed = { onEvent(CommercialLocalEvent.BarrioSuggestionsDismissed) },
            isError = state.barrioError != null,
            supportingText = state.barrioError,
        )
        OficiosTextField(
            value = state.calle,
            onValueChange = { onEvent(CommercialLocalEvent.CalleChanged(it)) },
            label = stringResource(R.string.commercial_local_street),
            isError = state.streetError != null,
            supportingText = state.streetError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OficiosTextField(
            value = state.numero,
            onValueChange = { onEvent(CommercialLocalEvent.NumeroChanged(it)) },
            label = stringResource(R.string.commercial_local_number),
            isError = state.numberError != null,
            supportingText = state.numberError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
        )
        OficiosAddressLocationPicker(
            latitude = state.location.latitude,
            longitude = state.location.longitude,
            showMap = state.location.showMap,
            pinDraggable = state.location.pinDraggable,
            showLocateOnMap = state.location.showLocateOnMap,
            showConfirm = state.location.showConfirm,
            isGeocoding = state.isGeocoding,
            onLocateOnMap = { onEvent(CommercialLocalEvent.LocateOnMap) },
            onPinChanged = { latitude, longitude ->
                onEvent(CommercialLocalEvent.MapLocationSelected(latitude, longitude))
            },
            onConfirmPin = { onEvent(CommercialLocalEvent.ConfirmPin) },
            locateOnMapLabel = state.location.locateOnMapLabel,
            confirmLabel = stringResource(R.string.commercial_local_confirm_pin),
            title = stringResource(R.string.commercial_local_map_title),
            hint = state.location.message,
        )
        OficiosButton(
            text = stringResource(R.string.commercial_local_save),
            onClick = { onEvent(CommercialLocalEvent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
            enabled = state.canSubmit && !state.isSaving && !state.isGeocoding,
            loading = state.isSaving || state.isGeocoding,
        )
    }
}
