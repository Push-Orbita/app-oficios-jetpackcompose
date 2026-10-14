package com.cocido.oficios.feature.settings.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPhoneField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.inputs.PhoneCountryOptions
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
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
        ClientDataMode.LIST -> "Contactos y domicilios"
        ClientDataMode.CONTACT -> if (uiState.editingId == null) "Nuevo contacto" else "Editar contacto"
        ClientDataMode.DOMICILIO -> if (uiState.editingId == null) "Nuevo domicilio" else "Editar domicilio"
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
        when {
            uiState.isLoading && uiState.contacts.isEmpty() && uiState.domicilios.isEmpty() ->
                OficiosLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            uiState.errorMessage != null && uiState.personaId == null && uiState.mode == ClientDataMode.LIST ->
                OficiosErrorState(
                    title = "No pudimos cargar tus datos",
                    description = uiState.errorMessage,
                    onRetry = { onEvent(ClientDataEvent.Retry) },
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            uiState.mode == ClientDataMode.LIST -> ClientDataList(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
            uiState.mode == ClientDataMode.CONTACT -> ContactEditor(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
            uiState.mode == ClientDataMode.DOMICILIO -> DomicilioEditor(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
        }
    }
    uiState.pendingDelete?.let { target ->
        OficiosDialog(
            title = "Eliminar ${target.label}",
            message = "Esta acción puede dejar tu perfil incompleto. Los pedidos existentes conservarán sus datos históricos.",
            confirmLabel = "Eliminar",
            isDangerous = true,
            onConfirm = { onEvent(ClientDataEvent.ConfirmDelete) },
            onDismiss = { onEvent(ClientDataEvent.DismissDelete) },
        )
    }
}

@Composable
private fun ClientDataList(
    uiState: ClientDataUiState,
    onEvent: (ClientDataEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = "Podés guardar varias opciones. Marcá una como principal para usarla por defecto al contratar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth),
            )
        }
        uiState.errorMessage?.let { message -> item { OficiosInlineAlert(message) } }
        item {
            SectionHeader(
                title = "Contactos",
                actionLabel = "Agregar",
                onAction = { onEvent(ClientDataEvent.AddContact) },
            )
        }
        if (uiState.contacts.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = "Todavía no tenés contactos",
                    description = "Agregá un WhatsApp para coordinar los servicios.",
                    ctaLabel = "Agregar contacto",
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
                title = "Domicilios",
                actionLabel = "Agregar",
                onAction = { onEvent(ClientDataEvent.AddDomicilio) },
            )
        }
        if (uiState.domicilios.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = "Todavía no tenés domicilios",
                    description = "Agregá dónde querés recibir el servicio.",
                    ctaLabel = "Agregar domicilio",
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
                Text(contact.telefonoWhatsapp, style = MaterialTheme.typography.bodyMedium, color = OficiosColorSecondaryText)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (contact.telefonoVerificado) Icons.Outlined.Verified else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = if (contact.telefonoVerificado) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = if (contact.telefonoVerificado) "WhatsApp verificado" else "Falta verificar WhatsApp",
                        style = MaterialTheme.typography.labelMedium,
                        color = OficiosColorSecondaryText,
                    )
                }
            }
            IconButton(onClick = { onEvent(ClientDataEvent.ToggleContactFavorite(contact.id, !contact.favorito)) }) {
                Icon(if (contact.favorito) Icons.Outlined.Star else Icons.Outlined.StarBorder, "Contacto principal")
            }
            IconButton(onClick = { onEvent(ClientDataEvent.EditContact(contact)) }) {
                Icon(Icons.Outlined.Edit, "Editar contacto")
            }
            IconButton(onClick = {
                onEvent(ClientDataEvent.RequestDelete(ClientDataDeleteTarget.Contact(contact.id, "el contacto")))
            }) {
                Icon(Icons.Outlined.DeleteOutline, "Eliminar contacto", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DomicilioCard(domicilio: ClientDomicilio, onEvent: (ClientDataEvent) -> Unit) {
    val title = domicilio.nombre?.takeIf { it.isNotBlank() } ?: domicilio.barrioName ?: "Domicilio"
    val detail = listOfNotNull(
        domicilio.calle,
        domicilio.numero,
        domicilio.piso?.let { "Piso $it" },
        domicilio.departamento?.let { "Dpto. $it" },
    ).filter { it.isNotBlank() }.joinToString(" ")
    OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(detail.ifBlank { "Sin detalle de calle" }, style = MaterialTheme.typography.bodyMedium, color = OficiosColorSecondaryText)
                domicilio.barrioName?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = OficiosColorSecondaryText)
                }
            }
            IconButton(onClick = { onEvent(ClientDataEvent.ToggleDomicilioFavorite(domicilio.id, !domicilio.favorito)) }) {
                Icon(if (domicilio.favorito) Icons.Outlined.Star else Icons.Outlined.StarBorder, "Domicilio principal")
            }
            IconButton(onClick = { onEvent(ClientDataEvent.EditDomicilio(domicilio)) }) {
                Icon(Icons.Outlined.Edit, "Editar domicilio")
            }
            IconButton(onClick = {
                onEvent(ClientDataEvent.RequestDelete(ClientDataDeleteTarget.Domicilio(domicilio.id, "el domicilio")))
            }) {
                Icon(Icons.Outlined.DeleteOutline, "Eliminar domicilio", tint = MaterialTheme.colorScheme.error)
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
            .consumeWindowInsets(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            "El número se valida por WhatsApp. Argentina usa +54 y vos ingresás solo el número local, por ejemplo 9 370 5219109.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosTextField(
            value = uiState.contactName,
            onValueChange = { onEvent(ClientDataEvent.ContactNameChanged(it)) },
            label = "Nombre del contacto",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
        )
        OficiosPhoneField(
            value = uiState.contactPhone,
            onValueChange = { onEvent(ClientDataEvent.ContactPhoneChanged(it)) },
            selectedCountry = PhoneCountryOptions.ARGENTINA,
            label = "WhatsApp",
            supportingText = "11 dígitos con el 9",
            numberModifier = Modifier.focusRequester(phoneFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
        )
        OficiosTextField(
            value = uiState.contactEmail,
            onValueChange = { onEvent(ClientDataEvent.ContactEmailChanged(it)) },
            label = "Correo alternativo (opcional)",
            modifier = Modifier.focusRequester(emailFocus),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onEvent(ClientDataEvent.SaveContact) }),
        )
        OficiosCheckbox(
            checked = uiState.contactFavorite,
            onCheckedChange = { onEvent(ClientDataEvent.ContactFavoriteChanged(it)) },
            label = "Usar como contacto principal",
        )
        uiState.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = if (uiState.editingId == null) "Guardar y verificar" else "Guardar cambios",
            onClick = { onEvent(ClientDataEvent.SaveContact) },
            loading = uiState.isSaving,
            enabled = !uiState.isSaving,
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
            .consumeWindowInsets(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            "Guardá una etiqueta clara, como “Casa” o “Trabajo”, y confirmá la entrada en el mapa para evitar confusiones.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosTextField(
            value = uiState.addressName,
            onValueChange = { onEvent(ClientDataEvent.AddressNameChanged(it)) },
            label = "Nombre (opcional)",
            placeholder = "Ej: Casa",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { barrioFocus.requestFocus() }),
        )
        OficiosAutocompleteField(
            value = uiState.barrioQuery,
            onValueChange = { onEvent(ClientDataEvent.BarrioQueryChanged(it)) },
            suggestions = uiState.barrioSuggestions,
            onSuggestionSelected = { onEvent(ClientDataEvent.BarrioSelected(it)) },
            suggestionLabel = { it.label },
            label = "Barrio",
            placeholder = "Buscá y seleccioná tu barrio",
            modifier = Modifier.focusRequester(barrioFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { streetFocus.requestFocus() }),
        )
        OficiosTextField(
            value = uiState.street,
            onValueChange = { onEvent(ClientDataEvent.StreetChanged(it)) },
            label = "Calle",
            modifier = Modifier.focusRequester(streetFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { numberFocus.requestFocus() }),
        )
        OficiosTextField(
            value = uiState.number,
            onValueChange = { onEvent(ClientDataEvent.NumberChanged(it)) },
            label = "Altura",
            modifier = Modifier.focusRequester(numberFocus),
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
                    label = "Piso",
                    modifier = Modifier.focusRequester(floorFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { apartmentFocus.requestFocus() }),
                )
            }
            Column(Modifier.weight(1f)) {
                OficiosTextField(
                    value = uiState.apartment,
                    onValueChange = { onEvent(ClientDataEvent.ApartmentChanged(it)) },
                    label = "Dpto.",
                    modifier = Modifier.focusRequester(apartmentFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { towerFocus.requestFocus() }),
                )
            }
        }
        OficiosTextField(
            value = uiState.houseTower,
            onValueChange = { onEvent(ClientDataEvent.HouseTowerChanged(it)) },
            label = "Casa o torre (opcional)",
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
            label = "Usar como domicilio principal",
        )
        uiState.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = "Guardar domicilio",
            onClick = { onEvent(ClientDataEvent.SaveDomicilio) },
            loading = uiState.isSaving,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}
