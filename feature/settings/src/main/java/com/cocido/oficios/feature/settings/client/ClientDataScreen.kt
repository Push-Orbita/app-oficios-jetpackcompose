package com.cocido.oficios.feature.settings.client

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPhoneField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOptions
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosLocationPicker
import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientDomicilio

@Composable
fun ClientDataScreen(
    uiState: ClientDataUiState,
    onEvent: (ClientDataEvent) -> Unit,
) {
    val title = when (uiState.mode) {
        ClientDataMode.LIST -> stringResource(R.string.client_data_title)
        ClientDataMode.CONTACT -> if (uiState.editingId == null) stringResource(R.string.client_data_new_contact) else stringResource(R.string.client_data_edit_contact)
        ClientDataMode.DOMICILIO -> if (uiState.editingId == null) stringResource(R.string.client_data_new_address) else stringResource(R.string.client_data_edit_address)
    }
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = title,
                onBack = {
                    if (uiState.mode == ClientDataMode.LIST) onEvent(ClientDataEvent.Back)
                    else onEvent(ClientDataEvent.CloseEditor)
                },
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.client_data_load_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(ClientDataEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> when (state.mode) {
                    ClientDataMode.LIST -> ClientDataList(uiState = state, onEvent = onEvent)
                    ClientDataMode.CONTACT -> ContactEditor(uiState = state, onEvent = onEvent)
                    ClientDataMode.DOMICILIO -> DomicilioEditor(uiState = state, onEvent = onEvent)
                }
            }
        }
    }
    uiState.pendingDelete?.let { target ->
        OficiosDialog(
            title = stringResource(R.string.client_data_delete_title, target.label),
            message = stringResource(R.string.client_data_delete_message),
            confirmLabel = stringResource(R.string.client_data_delete_confirm),
            isDangerous = true,
            onConfirm = { onEvent(ClientDataEvent.ConfirmDelete) },
            onDismiss = { onEvent(ClientDataEvent.DismissDelete) },
        )
    }
}

private fun ClientDataUiState.phase(): OficiosContentPhase = when {
    isLoading && contacts.isEmpty() && domicilios.isEmpty() -> OficiosContentPhase.Loading
    errorMessage != null && personaId == null && mode == ClientDataMode.LIST -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun ClientDataList(
    uiState: ClientDataUiState,
    onEvent: (ClientDataEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(R.string.client_data_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth),
            )
        }
        uiState.errorMessage?.let { message -> item { OficiosInlineAlert(message) } }
        item {
            SectionHeader(
                title = stringResource(R.string.client_data_contacts),
                actionLabel = stringResource(R.string.client_data_add),
                onAction = { onEvent(ClientDataEvent.AddContact) },
            )
        }
        if (uiState.contacts.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = stringResource(R.string.client_data_contacts_empty_title),
                    description = stringResource(R.string.client_data_contacts_empty_description),
                    ctaLabel = stringResource(R.string.client_data_add_contact),
                    onCta = { onEvent(ClientDataEvent.AddContact) },
                )
            }
        } else {
            items(uiState.contacts, key = { "contact-${it.id}" }) { contact ->
                ContactCard(contact, onEvent)
            }
        }
        item {
            SectionHeader(
                title = stringResource(R.string.client_data_addresses),
                actionLabel = stringResource(R.string.client_data_add),
                onAction = { onEvent(ClientDataEvent.AddDomicilio) },
            )
        }
        if (uiState.domicilios.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = stringResource(R.string.client_data_addresses_empty_title),
                    description = stringResource(R.string.client_data_addresses_empty_description),
                    ctaLabel = stringResource(R.string.client_data_add_address),
                    onCta = { onEvent(ClientDataEvent.AddDomicilio) },
                )
            }
        } else {
            items(uiState.domicilios, key = { "address-${it.id}" }) { domicilio ->
                DomicilioCard(domicilio, onEvent)
            }
        }
        item { Column(Modifier.padding(bottom = OficiosSpacing.xl)) {} }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        OficiosButton(
            text = actionLabel,
            onClick = onAction,
            leadingIcon = Icons.Outlined.Add,
            variant = OficiosButtonVariant.Ghost,
        )
    }
}

@Composable
private fun ContactCard(contact: ClientContacto, onEvent: (ClientDataEvent) -> Unit) {
    OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
                Text(contact.nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(contact.telefonoWhatsapp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (contact.telefonoVerificado) OficiosIcons.ShieldCheck else OficiosIcons.CircleAlert
                        ),
                        contentDescription = null,
                        tint = if (contact.telefonoVerificado) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = if (contact.telefonoVerificado) stringResource(R.string.client_data_whatsapp_verified) else stringResource(R.string.client_data_whatsapp_pending),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = { onEvent(ClientDataEvent.ToggleContactFavorite(contact.id, !contact.favorito)) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.Star),
                    contentDescription = stringResource(R.string.client_data_primary_contact),
                    tint = if (contact.favorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = { onEvent(ClientDataEvent.EditContact(contact)) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.Pencil),
                    contentDescription = stringResource(R.string.client_data_edit_contact)
                )
            }
            val deleteContactLabel = stringResource(R.string.client_data_delete_contact)
            IconButton(onClick = {
                onEvent(ClientDataEvent.RequestDelete(ClientDataDeleteTarget.Contact(contact.id, deleteContactLabel)))
            }) {
                Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.client_data_delete_contact_action), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DomicilioCard(domicilio: ClientDomicilio, onEvent: (ClientDataEvent) -> Unit) {
    val title = domicilio.nombre?.takeIf { it.isNotBlank() } ?: domicilio.barrioName ?: stringResource(R.string.client_data_address_fallback)
    val piso = domicilio.piso
    val departamento = domicilio.departamento
    val floor = if (piso.isNullOrBlank()) null else stringResource(R.string.client_data_floor, piso)
    val apartment = if (departamento.isNullOrBlank()) null else stringResource(R.string.client_data_apartment, departamento)
    val detail = listOfNotNull(
        domicilio.calle,
        domicilio.numero,
        floor,
        apartment,
    ).filter { it.isNotBlank() }.joinToString(" ")
    OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(detail.ifBlank { stringResource(R.string.client_data_no_street) }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                domicilio.barrioName?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = { onEvent(ClientDataEvent.ToggleDomicilioFavorite(domicilio.id, !domicilio.favorito)) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.Star),
                    contentDescription = stringResource(R.string.client_data_primary_address),
                    tint = if (domicilio.favorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = { onEvent(ClientDataEvent.EditDomicilio(domicilio)) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(OficiosIcons.Pencil),
                    contentDescription = stringResource(R.string.client_data_edit_address)
                )
            }
            val deleteAddressLabel = stringResource(R.string.client_data_delete_address)
            IconButton(onClick = {
                onEvent(ClientDataEvent.RequestDelete(ClientDataDeleteTarget.Domicilio(domicilio.id, deleteAddressLabel)))
            }) {
                Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.client_data_delete_address_action), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ContactEditor(
    uiState: ClientDataUiState,
    onEvent: (ClientDataEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phoneFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            stringResource(R.string.client_data_phone_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosTextField(
            value = uiState.contactName,
            onValueChange = { onEvent(ClientDataEvent.ContactNameChanged(it)) },
            label = stringResource(R.string.client_data_contact_name),
            isError = uiState.contactNameError != null,
            supportingText = uiState.contactNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
        )
        OficiosPhoneField(
            value = uiState.contactPhone,
            onValueChange = { onEvent(ClientDataEvent.ContactPhoneChanged(it)) },
            selectedCountry = PhoneCountryOptions.ARGENTINA,
            label = stringResource(R.string.client_data_whatsapp),
            isError = uiState.contactPhoneError != null,
            supportingText = uiState.contactPhoneError
                ?: "Ingrese el número local, con el 9 de móvil.",
            numberModifier = Modifier.focusRequester(phoneFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
        )
        OficiosTextField(
            value = uiState.contactEmail,
            onValueChange = { onEvent(ClientDataEvent.ContactEmailChanged(it)) },
            label = stringResource(R.string.client_data_alt_email),
            modifier = Modifier.focusRequester(emailFocus),
            isError = uiState.contactEmailError != null,
            supportingText = uiState.contactEmailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onEvent(ClientDataEvent.SaveContact) }),
        )
        OficiosCheckbox(
            checked = uiState.contactFavorite,
            onCheckedChange = { onEvent(ClientDataEvent.ContactFavoriteChanged(it)) },
            label = stringResource(R.string.client_data_primary_contact_toggle),
        )
        uiState.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = if (uiState.editingId == null) stringResource(R.string.client_data_save_verify) else stringResource(R.string.client_data_save_changes),
            onClick = { onEvent(ClientDataEvent.SaveContact) },
            loading = uiState.isSaving,
            enabled = uiState.canSaveContact && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}

@Composable
private fun DomicilioEditor(
    uiState: ClientDataUiState,
    onEvent: (ClientDataEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barrioFocus = remember { FocusRequester() }
    val streetFocus = remember { FocusRequester() }
    val numberFocus = remember { FocusRequester() }
    val floorFocus = remember { FocusRequester() }
    val apartmentFocus = remember { FocusRequester() }
    val towerFocus = remember { FocusRequester() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            stringResource(R.string.client_data_address_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosTextField(
            value = uiState.addressName,
            onValueChange = { onEvent(ClientDataEvent.AddressNameChanged(it)) },
            label = stringResource(R.string.client_data_name_optional),
            placeholder = stringResource(R.string.client_data_name_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { barrioFocus.requestFocus() }),
        )
        OficiosAutocompleteField(
            value = uiState.barrioQuery,
            onValueChange = { onEvent(ClientDataEvent.BarrioQueryChanged(it)) },
            suggestions = uiState.barrioSuggestions,
            onSuggestionSelected = { onEvent(ClientDataEvent.BarrioSelected(it)) },
            suggestionLabel = { it.label },
            label = stringResource(R.string.client_data_neighborhood),
            placeholder = stringResource(R.string.client_data_neighborhood_hint),
            isLoading = uiState.isSearchingBarrios,
            onFocused = { onEvent(ClientDataEvent.BarrioFocused) },
            onSuggestionsDismissed = { onEvent(ClientDataEvent.BarrioSuggestionsDismissed) },
            modifier = Modifier.focusRequester(barrioFocus),
            isError = uiState.addressBarrioError != null,
            supportingText = uiState.addressBarrioError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { streetFocus.requestFocus() }),
        )
        OficiosTextField(
            value = uiState.street,
            onValueChange = { onEvent(ClientDataEvent.StreetChanged(it)) },
            label = stringResource(R.string.client_data_street),
            modifier = Modifier.focusRequester(streetFocus),
            isError = uiState.addressStreetError != null,
            supportingText = uiState.addressStreetError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { numberFocus.requestFocus() }),
        )
        OficiosTextField(
            value = uiState.number,
            onValueChange = { onEvent(ClientDataEvent.NumberChanged(it)) },
            label = stringResource(R.string.client_data_number),
            modifier = Modifier.focusRequester(numberFocus),
            isError = uiState.addressNumberError != null,
            supportingText = uiState.addressNumberError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { floorFocus.requestFocus() }),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Column(Modifier.weight(1f)) {
                OficiosTextField(
                    value = uiState.floor,
                    onValueChange = { onEvent(ClientDataEvent.FloorChanged(it)) },
                    label = stringResource(R.string.client_data_floor_label),
                    modifier = Modifier.focusRequester(floorFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { apartmentFocus.requestFocus() }),
                )
            }
            Column(Modifier.weight(1f)) {
                OficiosTextField(
                    value = uiState.apartment,
                    onValueChange = { onEvent(ClientDataEvent.ApartmentChanged(it)) },
                    label = stringResource(R.string.client_data_apartment_label),
                    modifier = Modifier.focusRequester(apartmentFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { towerFocus.requestFocus() }),
                )
            }
        }
        OficiosTextField(
            value = uiState.houseTower,
            onValueChange = { onEvent(ClientDataEvent.HouseTowerChanged(it)) },
            label = stringResource(R.string.client_data_tower),
            modifier = Modifier.focusRequester(towerFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEvent(ClientDataEvent.SaveDomicilio) }),
        )
        OficiosLocationPicker(
            latitude = uiState.addressLatitude,
            longitude = uiState.addressLongitude,
            onLocationSelected = { latitude, longitude ->
                onEvent(ClientDataEvent.MapLocationSelected(latitude, longitude))
            },
            onUseCurrentLocation = { onEvent(ClientDataEvent.UseCurrentLocation) },
            onPermissionDenied = { onEvent(ClientDataEvent.LocationPermissionDenied) },
            enabled = !uiState.isSaving,
            isLocating = uiState.isLocating,
        )
        OficiosCheckbox(
            checked = uiState.addressFavorite,
            onCheckedChange = { onEvent(ClientDataEvent.AddressFavoriteChanged(it)) },
            label = stringResource(R.string.client_data_primary_address_toggle),
        )
        uiState.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.client_data_save_address),
            onClick = { onEvent(ClientDataEvent.SaveDomicilio) },
            loading = uiState.isSaving,
            enabled = uiState.canSaveDomicilio && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}
