package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.interaction.OficiosTouchTarget
import com.cocido.oficios.core.designsystem.component.interaction.oficiosMinTouchTarget
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosLocationPicker
import com.cocido.oficios.domain.model.ClientDomicilio
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.feature.home.components.ClientHomeBottomBar
import com.cocido.oficios.feature.home.components.ClientHomeTab

@Composable
fun MisUbicacionesScreen(
    uiState: MisUbicacionesUiState,
    onEvent: (MisUbicacionesEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: ClientHomeTab = ClientHomeTab.INICIO,
    onTabSelected: (ClientHomeTab) -> Unit = {},
    showFavorites: Boolean? = true,
) {
    val adding = uiState is MisUbicacionesUiState.Success &&
        uiState.mode == MisUbicacionesMode.ADD
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.locations_title),
                onBack = {
                    if (adding) onEvent(MisUbicacionesEvent.CancelAdd) else onNavigateBack()
                },
            )
        },
        bottomBar = {
            ClientHomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                showFavorites = showFavorites,
            )
        },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                MisUbicacionesUiState.Loading -> OficiosLoadingState(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.locations_loading),
                )
                is MisUbicacionesUiState.Error -> OficiosErrorState(
                    title = stringResource(R.string.locations_error_title),
                    description = state.message.ifBlank { stringResource(R.string.locations_error_description) },
                    onRetry = { onEvent(MisUbicacionesEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                is MisUbicacionesUiState.Success -> if (state.mode == MisUbicacionesMode.ADD) {
                    MisUbicacionesAddForm(
                        state = state,
                        onEvent = onEvent,
                    )
                } else {
                    MisUbicacionesPicker(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun MisUbicacionesPicker(
    state: MisUbicacionesUiState.Success,
    onEvent: (MisUbicacionesEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = state.domicilios.firstOrNull { it.id == state.selectedId }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        OficiosTextField(
            value = selected?.pickerLabel().orEmpty(),
            onValueChange = {},
            label = "",
            floatingLabel = false,
            readOnly = true,
            placeholder = stringResource(R.string.locations_add_placeholder),
            trailingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.CirclePlus),
                    contentDescription = stringResource(R.string.locations_add_new),
                    tint = OficiosColorWhite,
                    modifier = Modifier
                        .size(OficiosComponentSize.iconMd)
                        .clickable { onEvent(MisUbicacionesEvent.AddClicked) },
                )
            },
        )
        if (state.domicilios.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(MisUbicacionesEvent.MarkOnMapClicked) }
                    .oficiosMinTouchTarget(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.locations_mark_map),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OficiosColorWhite,
                )
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.MapPinHouse),
                    contentDescription = stringResource(R.string.locations_mark_map),
                    tint = OficiosColorWhite,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
            LocationPickerList(
                domicilios = state.domicilios,
                selectedId = state.selectedId,
                onSelected = { onEvent(MisUbicacionesEvent.LocationSelected(it)) },
                onFavorite = { id, favorite -> onEvent(MisUbicacionesEvent.FavoriteToggled(id, favorite)) },
                onAdd = { onEvent(MisUbicacionesEvent.AddClicked) },
            )
        }
        Text(
            text = stringResource(R.string.locations_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = OficiosColorTextSecondaryAccessible,
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            OficiosButton(
                text = stringResource(R.string.locations_save),
                onClick = { onEvent(MisUbicacionesEvent.Confirm) },
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Small,
                enabled = state.selectedId != null && !state.isSaving,
            )
        }
    }
}

@Composable
private fun LocationPickerList(
    domicilios: List<ClientDomicilio>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    onFavorite: (Long, Boolean) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OficiosColorOutlineFill, RoundedCornerShape(OficiosRadius.lg))
            .padding(OficiosSpacing.s10),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
    ) {
        domicilios.forEach { domicilio ->
            val selected = domicilio.id == selectedId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(OficiosRadius.sm))
                    .background(if (selected) OficiosColorWhite else OficiosColorBackground)
                    .clickable { onSelected(domicilio.id) }
                    .padding(OficiosSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = domicilio.pickerLabel(),
                    style = if (selected) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = if (selected) OficiosColorBackground else OficiosColorWhite,
                    modifier = Modifier.weight(1f),
                )
                val favoriteLabel = stringResource(
                    if (domicilio.favorito) R.string.locations_favorite else R.string.locations_unfavorite,
                )
                OficiosTouchTarget(
                    onClick = { onFavorite(domicilio.id, !domicilio.favorito) },
                    accessibilityContentDescription = favoriteLabel,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(OficiosIcons.Star),
                        contentDescription = null,
                        tint = when {
                            !domicilio.favorito -> OficiosColorWhite.copy(alpha = 0.45f)
                            selected -> OficiosColorBackground
                            else -> OficiosColorWhite
                        },
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(OficiosRadius.sm))
                .clickable(onClick = onAdd)
                .padding(OficiosSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.locations_add_new),
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorWhite,
            )
            Icon(
                imageVector = ImageVector.vectorResource(OficiosIcons.CirclePlus),
                contentDescription = null,
                tint = OficiosColorWhite,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
        }
    }
}

@Composable
private fun MisUbicacionesAddForm(
    state: MisUbicacionesUiState.Success,
    onEvent: (MisUbicacionesEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.OficiosScreenPadding),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.screenGap),
    ) {
        OficiosAutocompleteField(
            value = state.barrioQuery,
            onValueChange = { onEvent(MisUbicacionesEvent.BarrioQueryChanged(it)) },
            suggestions = state.barrioSuggestions,
            onSuggestionSelected = { onEvent(MisUbicacionesEvent.BarrioSelected(it)) },
            suggestionLabel = { it.label },
            label = stringResource(R.string.locations_neighborhood),
            isLoading = state.isSearchingBarrios,
            onFocused = { onEvent(MisUbicacionesEvent.BarrioFocused) },
            onSuggestionsDismissed = { onEvent(MisUbicacionesEvent.BarrioSuggestionsDismissed) },
        )
        OficiosTextField(
            value = state.street,
            onValueChange = { onEvent(MisUbicacionesEvent.StreetChanged(it)) },
            label = stringResource(R.string.locations_street),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OficiosTextField(
            value = state.number,
            onValueChange = { onEvent(MisUbicacionesEvent.NumberChanged(it)) },
            label = stringResource(R.string.locations_number),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
        )
        OficiosLocationPicker(
            latitude = state.addressLatitude,
            longitude = state.addressLongitude,
            onLocationSelected = { latitude, longitude ->
                onEvent(MisUbicacionesEvent.MapLocationSelected(latitude, longitude))
            },
            onUseCurrentLocation = { onEvent(MisUbicacionesEvent.UseCurrentLocation) },
            onPermissionDenied = { onEvent(MisUbicacionesEvent.LocationPermissionDenied) },
            enabled = !state.isSaving,
            isLocating = state.isLocating,
        )
        OficiosCheckbox(
            checked = state.addressFavorite,
            onCheckedChange = { onEvent(MisUbicacionesEvent.FavoriteNewChanged(it)) },
            label = stringResource(R.string.locations_primary_toggle),
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            OficiosButton(
                text = stringResource(R.string.locations_save),
                onClick = { onEvent(MisUbicacionesEvent.SaveNew) },
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Small,
                loading = state.isSaving,
                enabled = !state.isSaving,
            )
        }
    }
}
