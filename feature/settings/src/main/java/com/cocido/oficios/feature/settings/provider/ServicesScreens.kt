package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.OficiosDialog
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
import com.cocido.oficios.core.designsystem.component.categories.CategoryGridItem
import com.cocido.oficios.core.designsystem.component.categories.OficiosCategoriesGrid
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosLocationPicker
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ProviderPaymentMethods

@Composable
fun ServicesScreen(uiState: ServicesUiState, onEvent: (ServicesEvent) -> Unit) {
    OficiosScaffold(
        topBar = { OficiosBackTopBar(stringResource(R.string.services_title), onBack = { onEvent(ServicesEvent.Back) }) },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.services_load_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(ServicesEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> ServicesList(state = state, onEvent = onEvent)
            }
        }
    }
    uiState.pendingDelete?.let { service ->
        OficiosDialog(
            title = stringResource(R.string.services_delete_title, service.rubroName),
            message = stringResource(R.string.services_delete_message),
            confirmLabel = stringResource(R.string.services_delete_confirm),
            isDangerous = true,
            onConfirm = { onEvent(ServicesEvent.ConfirmDelete) },
            onDismiss = { onEvent(ServicesEvent.DismissDelete) },
        )
    }
}

private fun ServicesUiState.phase(): OficiosContentPhase = when {
    isLoading && services.isEmpty() -> OficiosContentPhase.Loading
    errorMessage != null && services.isEmpty() -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun ServicesList(state: ServicesUiState, onEvent: (ServicesEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        item {
            Text(
                stringResource(R.string.services_intro),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.errorMessage?.let { message -> item { OficiosInlineAlert(message) } }
        if (state.services.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = stringResource(R.string.services_empty_title),
                    description = stringResource(R.string.services_empty_description),
                    ctaLabel = stringResource(R.string.services_add),
                    onCta = { onEvent(ServicesEvent.AddService) },
                )
            }
        } else {
            items(state.services, key = { it.id }) { service ->
                ServiceCard(service, state.isDeleting, onEvent)
            }
            item {
                OficiosButton(
                    text = stringResource(R.string.services_add_another),
                    leadingIcon = Icons.Outlined.Add,
                    onClick = { onEvent(ServicesEvent.AddService) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Large,
                )
            }
        }
        item { Column(Modifier.padding(bottom = OficiosSpacing.xl)) {} }
    }
}

@Composable
private fun ServiceCard(service: ManagedProviderService, disabled: Boolean, onEvent: (ServicesEvent) -> Unit) {
    OficiosCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEvent(ServicesEvent.EditService(service)) },
        enabled = !disabled,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Column(Modifier.weight(1f).padding(horizontal = OficiosSpacing.sm), verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                Text(service.rubroName, style = MaterialTheme.typography.titleMedium)
                if (service.isPrimary) Text(stringResource(R.string.services_primary), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                Text(
                    service.subServices.joinToString { it.name }.ifBlank { stringResource(R.string.services_no_specialties) },
                    style = MaterialTheme.typography.bodyMedium,
                )
                val modalities = buildList {
                    if (service.subServices.any { it.servesAtClientHome }) add(stringResource(R.string.services_at_home))
                    if (service.subServices.any { it.servesAtLocal }) add(stringResource(R.string.services_at_local))
                    if (service.subServices.any { it.requiresTurns }) add(stringResource(R.string.services_with_turns))
                }
                Text(modalities.joinToString(" · ").ifBlank { stringResource(R.string.services_modality_unknown) }, style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
                if (service.workSchedule.isNotEmpty()) {
                    val days = dayOptions()
                    val scheduleLines = mutableListOf<String>()
                    for (schedule in service.workSchedule) {
                        val from = days.firstOrNull { it.first == schedule.dayFrom }?.second ?: schedule.dayFrom
                        val to = days.firstOrNull { it.first == schedule.dayTo }?.second ?: schedule.dayTo
                        val label = if (schedule.dayFrom != schedule.dayTo) {
                            stringResource(R.string.services_day_range, from, to.lowercase())
                        } else {
                            from
                        }
                        scheduleLines += "$label ${schedule.ranges.joinToString { "${it.from}–${it.to}" }}"
                    }
                    Text(
                        scheduleLines.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = OficiosColorSecondaryText,
                    )
                }
                if (service.isPrimary) {
                    Text(stringResource(R.string.services_primary_locked), style = MaterialTheme.typography.labelSmall, color = OficiosColorSecondaryText)
                } else {
                    Text(stringResource(R.string.services_tap_to_edit), style = MaterialTheme.typography.labelSmall, color = OficiosColorSecondaryText)
                }
            }
            if (!service.isPrimary) {
                IconButton(enabled = !disabled, onClick = { onEvent(ServicesEvent.RequestDelete(service)) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.services_delete_action, service.rubroName))
                }
            }
        }
    }
}

@Composable
fun AddServiceScreen(uiState: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(
                    if (uiState.isEditing) R.string.services_edit_title else R.string.services_add,
                ),
                onBack = { onEvent(AddServiceEvent.Previous) },
            )
        },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.services_form_error),
                    description = state.errorMessage.orEmpty(),
                    onRetry = { onEvent(AddServiceEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> AddServiceForm(state = state, onEvent = onEvent)
            }
        }
    }
}

private fun AddServiceUiState.phase(): OficiosContentPhase = when {
    isLoading && categories.isEmpty() -> OficiosContentPhase.Loading
    errorMessage != null && categories.isEmpty() -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun AddServiceForm(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(stringResource(R.string.services_step_counter, state.step.ordinal + 1, AddServiceStep.entries.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
        Text(stepTitle(state.step), style = MaterialTheme.typography.headlineSmall)
        Text(stepDescription(state.step), style = MaterialTheme.typography.bodyMedium)
        when (state.step) {
            AddServiceStep.TRADE -> TradeStep(state, onEvent)
            AddServiceStep.MODALITY -> ModalityStep(state, onEvent)
            AddServiceStep.SCHEDULE -> ScheduleStep(state, onEvent)
            AddServiceStep.PAYMENT -> PaymentStep(state, onEvent)
            AddServiceStep.REVIEW -> ReviewStep(state)
        }
        state.errorMessage?.let { OficiosInlineAlert(it) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            if (state.step != AddServiceStep.TRADE) {
                OficiosButton(stringResource(R.string.services_back), { onEvent(AddServiceEvent.Previous) }, Modifier.weight(1f), OficiosButtonVariant.Outlined)
            }
            OficiosButton(
                text = when {
                    state.step != AddServiceStep.REVIEW -> stringResource(R.string.services_continue)
                    state.isEditing -> stringResource(R.string.services_save_changes)
                    else -> stringResource(R.string.services_publish)
                },
                onClick = { onEvent(if (state.step == AddServiceStep.REVIEW) AddServiceEvent.Save else AddServiceEvent.Next) },
                modifier = Modifier.weight(1f),
                loading = state.isSaving,
                enabled = !state.isSaving,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
            )
        }
        Column(Modifier.padding(bottom = OficiosSpacing.lg)) {}
    }
}

@Composable
private fun TradeStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    if (state.isEditing) {
        Text(
            text = stringResource(R.string.services_edit_trade_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.services_edit_trade_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(state.selectedRubroLabel, style = MaterialTheme.typography.titleMedium)
        if (state.isPrimaryService) {
            Text(
                text = stringResource(R.string.services_primary_rubro_locked),
                style = MaterialTheme.typography.bodySmall,
                color = OficiosColorSecondaryText,
            )
        }
        if (state.subRubros.isNotEmpty()) {
            Text(stringResource(R.string.services_specialties), style = MaterialTheme.typography.titleMedium)
            state.subRubros.forEach { option ->
                OficiosCheckbox(
                    option.id in state.selectedSubRubroIds,
                    { onEvent(AddServiceEvent.SubRubroToggled(option.id, it)) },
                    option.label,
                )
            }
        }
        return
    }
    Text(
        text = stringResource(R.string.services_elegi_rubro_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Text(
        text = stringResource(R.string.services_elegi_rubro_hint),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (state.selectedCategoryId == null) {
        Text(stringResource(R.string.services_category), style = MaterialTheme.typography.titleMedium)
        OficiosCategoriesGrid(
            categories = state.categories.map { CategoryGridItem(it.id, it.label) },
            onCategoryClick = { onEvent(AddServiceEvent.CategorySelected(it.id)) },
        )
    } else {
        Text(stringResource(R.string.services_trade), style = MaterialTheme.typography.titleMedium)
        OficiosCategoriesGrid(
            categories = state.rubros.map { CategoryGridItem(it.id, it.label) },
            onCategoryClick = { onEvent(AddServiceEvent.RubroSelected(it.id)) },
            selectedCategoryId = state.selectedRubroId,
        )
        if (state.subRubros.isNotEmpty()) {
            Text(stringResource(R.string.services_specialties), style = MaterialTheme.typography.titleMedium)
            state.subRubros.forEach { option ->
                OficiosCheckbox(
                    option.id in state.selectedSubRubroIds,
                    { onEvent(AddServiceEvent.SubRubroToggled(option.id, it)) },
                    option.label,
                )
            }
        }
    }
}

@Composable
private fun ModalityStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    OficiosCheckbox(state.servesAtClientHome, { onEvent(AddServiceEvent.ClientHomeChanged(it)) }, stringResource(R.string.services_client_home))
    OficiosCheckbox(state.servesAtLocal, { onEvent(AddServiceEvent.LocalChanged(it)) }, stringResource(R.string.services_local))
    OficiosCheckbox(state.requiresTurns, { onEvent(AddServiceEvent.RequiresTurnsChanged(it)) }, stringResource(R.string.services_requires_turns))
    if (state.servesAtLocal) {
        Text(stringResource(R.string.services_local_address), style = MaterialTheme.typography.titleMedium)
        OficiosTextField(state.localName, { onEvent(AddServiceEvent.LocalNameChanged(it)) }, stringResource(R.string.services_local_name), placeholder = stringResource(R.string.services_local_name_hint))
        OficiosAutocompleteField(
            value = state.localBarrioQuery,
            onValueChange = { onEvent(AddServiceEvent.LocalBarrioChanged(it)) },
            suggestions = state.localBarrioSuggestions,
            onSuggestionSelected = { onEvent(AddServiceEvent.LocalBarrioSelected(it)) },
            suggestionLabel = { it.label },
            label = stringResource(R.string.services_neighborhood),
            placeholder = stringResource(R.string.services_neighborhood_hint),
        )
        OficiosTextField(state.localStreet, { onEvent(AddServiceEvent.LocalStreetChanged(it)) }, stringResource(R.string.services_street))
        OficiosLocationPicker(
            latitude = state.localLatitude,
            longitude = state.localLongitude,
            onLocationSelected = { latitude, longitude ->
                onEvent(AddServiceEvent.LocalMapLocationSelected(latitude, longitude))
            },
            onUseCurrentLocation = { onEvent(AddServiceEvent.UseCurrentLocation) },
            onPermissionDenied = { onEvent(AddServiceEvent.LocationPermissionDenied) },
            isLocating = state.isLocating,
        )
    }
}

@Composable
private fun ScheduleStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    Text(stringResource(R.string.services_days), style = MaterialTheme.typography.titleMedium)
    dayOptions().forEach { (id, label) -> OficiosCheckbox(id in state.workDays, { onEvent(AddServiceEvent.WorkDayToggled(id, it)) }, label, compactRow = true) }
    Text(stringResource(R.string.services_hours), style = MaterialTheme.typography.titleMedium)
    state.timeSlots.forEachIndexed { index, slot ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            OficiosDropdownField(slot.from, TIME_OPTIONS, { onEvent(AddServiceEvent.TimeFromChanged(index, it)) }, stringResource(R.string.services_from), Modifier.weight(1f))
            OficiosDropdownField(slot.to, TIME_OPTIONS, { onEvent(AddServiceEvent.TimeToChanged(index, it)) }, stringResource(R.string.services_to), Modifier.weight(1f))
            if (state.timeSlots.size > 1) IconButton(onClick = { onEvent(AddServiceEvent.RemoveTimeSlot(index)) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.services_remove_slot))
            }
        }
    }
    OficiosButton(stringResource(R.string.services_add_slot), { onEvent(AddServiceEvent.AddTimeSlot) }, Modifier.fillMaxWidth(), OficiosButtonVariant.Outlined, leadingIcon = Icons.Outlined.Add)
}

@Composable
private fun PaymentStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    paymentOptions().forEach { (id, label) -> OficiosCheckbox(id in state.paymentMethods, { onEvent(AddServiceEvent.PaymentToggled(id, it)) }, label) }
}

@Composable
private fun ReviewStep(state: AddServiceUiState) {
    OficiosCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Text(state.selectedRubroLabel, style = MaterialTheme.typography.titleLarge)
            Text(state.subRubros.filter { it.id in state.selectedSubRubroIds }.joinToString { it.label })
            Text(buildList { if (state.servesAtClientHome) add(stringResource(R.string.services_at_home)); if (state.servesAtLocal) add(stringResource(R.string.services_at_local)); if (state.requiresTurns) add(stringResource(R.string.services_with_turns)) }.joinToString(" · "))
            val days = dayOptions()
            val payments = paymentOptions()
            Text(state.workDays.joinToString { id -> days.firstOrNull { it.first == id }?.second ?: id })
            Text(state.timeSlots.joinToString { "${it.from}–${it.to}" })
            Text(state.paymentMethods.joinToString { id -> payments.firstOrNull { it.first == id }?.second ?: id })
            if (state.servesAtLocal) {
                Text(
                    if (state.localLatitude != null && state.localLongitude != null) stringResource(R.string.services_location_confirmed) else stringResource(R.string.services_location_pending),
                    style = MaterialTheme.typography.bodySmall,
                    color = OficiosColorSecondaryText,
                )
            }
        }
    }
    Text(stringResource(R.string.services_review_note), style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
}

@Composable
private fun stepTitle(step: AddServiceStep) = when (step) {
    AddServiceStep.TRADE -> stringResource(R.string.services_step_trade_title)
    AddServiceStep.MODALITY -> stringResource(R.string.services_step_modality_title)
    AddServiceStep.SCHEDULE -> stringResource(R.string.services_step_schedule_title)
    AddServiceStep.PAYMENT -> stringResource(R.string.services_step_payment_title)
    AddServiceStep.REVIEW -> stringResource(R.string.services_step_review_title)
}
@Composable
private fun stepDescription(step: AddServiceStep) = when (step) {
    AddServiceStep.TRADE -> stringResource(R.string.services_step_trade_body)
    AddServiceStep.MODALITY -> stringResource(R.string.services_step_modality_body)
    AddServiceStep.SCHEDULE -> stringResource(R.string.services_step_schedule_body)
    AddServiceStep.PAYMENT -> stringResource(R.string.services_step_payment_body)
    AddServiceStep.REVIEW -> stringResource(R.string.services_step_review_body)
}

private val TIME_OPTIONS = buildList { for (hour in 0..23) for (minute in listOf(0, 30)) add("%02d:%02d".format(hour, minute)) }

@Composable
private fun dayOptions() = listOf(
    "LUNES" to stringResource(R.string.services_day_monday),
    "MARTES" to stringResource(R.string.services_day_tuesday),
    "MIERCOLES" to stringResource(R.string.services_day_wednesday),
    "JUEVES" to stringResource(R.string.services_day_thursday),
    "VIERNES" to stringResource(R.string.services_day_friday),
    "SABADO" to stringResource(R.string.services_day_saturday),
    "DOMINGO" to stringResource(R.string.services_day_sunday),
)

@Composable
private fun paymentOptions() = listOf(
    ProviderPaymentMethods.EFECTIVO to stringResource(R.string.professional_profile_cash),
    ProviderPaymentMethods.TRANSFERENCIA to stringResource(R.string.professional_profile_transfer),
    ProviderPaymentMethods.MERCADOPAGO to stringResource(R.string.professional_profile_mercadopago),
)
