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
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
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
        when {
            uiState.isLoading && uiState.services.isEmpty() -> OficiosLoadingState(Modifier.fillMaxSize().padding(padding))
            uiState.errorMessage != null && uiState.services.isEmpty() -> OficiosErrorState(
                title = stringResource(R.string.services_load_error),
                description = uiState.errorMessage,
                onRetry = { onEvent(ServicesEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                item {
                    Text(
                        stringResource(R.string.services_intro),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                uiState.errorMessage?.let { message -> item { OficiosInlineAlert(message) } }
                if (uiState.services.isEmpty()) {
                    item {
                        OficiosEmptyState(
                            title = stringResource(R.string.services_empty_title),
                            description = stringResource(R.string.services_empty_description),
                            ctaLabel = stringResource(R.string.services_add),
                            onCta = { onEvent(ServicesEvent.AddService) },
                        )
                    }
                } else {
                    items(uiState.services, key = { it.id }) { service ->
                        ServiceCard(service, uiState.isDeleting, onEvent)
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

@Composable
private fun ServiceCard(service: ManagedProviderService, disabled: Boolean, onEvent: (ServicesEvent) -> Unit) {
    OficiosCard(Modifier.fillMaxWidth()) {
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
        topBar = { OficiosBackTopBar(stringResource(R.string.services_add), onBack = { onEvent(AddServiceEvent.Previous) }) },
    ) { padding ->
        if (uiState.isLoading && uiState.categories.isEmpty()) {
            OficiosLoadingState(Modifier.fillMaxSize().padding(padding))
        } else if (uiState.errorMessage != null && uiState.categories.isEmpty()) {
            OficiosErrorState(
                title = stringResource(R.string.services_form_error),
                description = uiState.errorMessage,
                onRetry = { onEvent(AddServiceEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                Text(stringResource(R.string.services_step_counter, uiState.step.ordinal + 1, AddServiceStep.entries.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                Text(stepTitle(uiState.step), style = MaterialTheme.typography.headlineSmall)
                Text(stepDescription(uiState.step), style = MaterialTheme.typography.bodyMedium)
                when (uiState.step) {
                    AddServiceStep.TRADE -> TradeStep(uiState, onEvent)
                    AddServiceStep.MODALITY -> ModalityStep(uiState, onEvent)
                    AddServiceStep.SCHEDULE -> ScheduleStep(uiState, onEvent)
                    AddServiceStep.PAYMENT -> PaymentStep(uiState, onEvent)
                    AddServiceStep.REVIEW -> ReviewStep(uiState)
                }
                uiState.errorMessage?.let { OficiosInlineAlert(it) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    if (uiState.step != AddServiceStep.TRADE) {
                        OficiosButton(stringResource(R.string.services_back), { onEvent(AddServiceEvent.Previous) }, Modifier.weight(1f), OficiosButtonVariant.Outlined)
                    }
                    OficiosButton(
                        text = if (uiState.step == AddServiceStep.REVIEW) stringResource(R.string.services_publish) else stringResource(R.string.services_continue),
                        onClick = { onEvent(if (uiState.step == AddServiceStep.REVIEW) AddServiceEvent.Save else AddServiceEvent.Next) },
                        modifier = Modifier.weight(1f),
                        loading = uiState.isSaving,
                        enabled = !uiState.isSaving,
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                    )
                }
                Column(Modifier.padding(bottom = OficiosSpacing.lg)) {}
            }
        }
    }
}

@Composable
private fun TradeStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    OficiosDropdownField(
        value = state.selectedCategoryLabel,
        options = state.categories.map { it.label },
        onOptionSelected = { label -> state.categories.firstOrNull { it.label == label }?.let { onEvent(AddServiceEvent.CategorySelected(it.id)) } },
        label = stringResource(R.string.services_category),
        placeholder = stringResource(R.string.services_category_hint),
    )
    OficiosDropdownField(
        value = state.selectedRubroLabel,
        options = state.rubros.map { it.label },
        onOptionSelected = { label -> state.rubros.firstOrNull { it.label == label }?.let { onEvent(AddServiceEvent.RubroSelected(it.id)) } },
        label = stringResource(R.string.services_trade),
        placeholder = if (state.selectedCategoryId == null) stringResource(R.string.services_trade_hint_locked) else stringResource(R.string.services_trade_hint),
        enabled = state.selectedCategoryId != null && !state.isLoading,
    )
    if (state.subRubros.isNotEmpty()) {
        Text(stringResource(R.string.services_specialties), style = MaterialTheme.typography.titleMedium)
        state.subRubros.forEach { option ->
            OficiosCheckbox(option.id in state.selectedSubRubroIds, { onEvent(AddServiceEvent.SubRubroToggled(option.id, it)) }, option.label)
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
