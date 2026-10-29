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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormActionsBar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosFormColumn
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.chips.OficiosFilterChip
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
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosAddressLocationPicker
import com.cocido.oficios.core.ui.navigation.rememberConfirmDiscardBackHandler
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleDayUi
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleEditor
import com.cocido.oficios.core.ui.schedule.WeeklyScheduleRangeUi
import com.cocido.oficios.domain.model.ManagedProviderService
import com.cocido.oficios.domain.model.ManagedWorkSchedule
import com.cocido.oficios.domain.model.ProviderPaymentMethods
import com.cocido.oficios.domain.model.WeeklyAvailability

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
                Text(modalities.joinToString(" · ").ifBlank { stringResource(R.string.services_modality_unknown) }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val days = dayOptions()
                if (service.specialtySchedules.size > 1) {
                    for (specialty in service.specialtySchedules) {
                        val hours = formatWorkSchedule(specialty.workSchedule, days)
                        if (hours.isNotBlank()) {
                            Text(
                                stringResource(R.string.services_specialty_hours, specialty.name, hours),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else if (service.workSchedule.isNotEmpty()) {
                    Text(
                        formatWorkSchedule(service.workSchedule, days),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (service.isPrimary) {
                    Text(stringResource(R.string.services_primary_locked), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(stringResource(R.string.services_tap_to_edit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    // Intra-wizard: Back vuelve al paso anterior. Descartar solo al salir del Paso 1.
    val leavingForm = uiState.step == AddServiceStep.TRADE
    val handleBack = rememberConfirmDiscardBackHandler(
        hasUnsavedChanges = leavingForm && uiState.hasUnsavedChanges,
        enabled = true,
        onConfirmExit = { onEvent(AddServiceEvent.Previous) },
    )
    OficiosScaffold(
        topBar = {
            OficiosBackTopBar(
                title = stringResource(
                    if (uiState.isEditing) R.string.services_edit_title else R.string.services_add,
                ),
                onBack = handleBack,
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

internal fun AddServiceUiState.phase(): OficiosContentPhase = when {
    isLoading && !hasFormContent -> OficiosContentPhase.Loading
    loadFailed && !hasFormContent -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun AddServiceForm(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    OficiosFormColumn(
        footer = {
            OficiosFormActionsBar(
                primaryLabel = when {
                    state.step != AddServiceStep.REVIEW -> stringResource(R.string.services_continue)
                    state.isEditing -> stringResource(R.string.services_save_changes)
                    else -> stringResource(R.string.services_publish)
                },
                onPrimary = {
                    onEvent(if (state.step == AddServiceStep.REVIEW) AddServiceEvent.Save else AddServiceEvent.Next)
                },
                backLabel = if (state.step != AddServiceStep.TRADE) {
                    stringResource(R.string.services_back)
                } else {
                    null
                },
                onBack = if (state.step != AddServiceStep.TRADE) {
                    { onEvent(AddServiceEvent.Previous) }
                } else {
                    null
                },
                primaryEnabled = !state.isSaving,
                primaryLoading = state.isSaving,
            )
        },
    ) {
        Text(stringResource(R.string.services_step_counter, state.step.ordinal + 1, AddServiceStep.entries.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
        Text(stepTitle(state.step, state.isEditing), style = MaterialTheme.typography.headlineSmall)
        Text(stepDescription(state.step, state.isEditing), style = MaterialTheme.typography.bodyMedium)
        when (state.step) {
            AddServiceStep.TRADE -> TradeStep(state, onEvent)
            AddServiceStep.MODALITY -> ModalityStep(state, onEvent)
            AddServiceStep.SCHEDULE -> ScheduleStep(state, onEvent)
            AddServiceStep.PAYMENT -> PaymentStep(state, onEvent)
            AddServiceStep.REVIEW -> ReviewStep(state)
        }
        state.errorMessage?.let { OficiosInlineAlert(it) }
    }
}

@Composable
private fun TradeStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    if (state.isEditing) {
        Text(state.selectedRubroLabel, style = MaterialTheme.typography.titleMedium)
        if (state.isPrimaryService) {
            Text(
                text = stringResource(R.string.services_primary_rubro_locked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(stringResource(R.string.services_specialties), style = MaterialTheme.typography.titleMedium)
        val published = state.subRubros.filter { it.id in state.selectedSubRubroIds }
        if (published.isEmpty()) {
            Text(
                stringResource(R.string.services_no_specialties),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            published.forEach { option ->
                Text(option.label, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text(
            stringResource(R.string.services_specialties_edit_locked),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            categories = state.categories.map {
                CategoryGridItem(id = it.id, label = it.label, iconUrl = it.iconUrl)
            },
            onCategoryClick = { onEvent(AddServiceEvent.CategorySelected(it.id)) },
        )
    } else {
        Text(stringResource(R.string.services_trade), style = MaterialTheme.typography.titleMedium)
        OficiosCategoriesGrid(
            categories = state.rubros.map {
                CategoryGridItem(id = it.id, label = it.label, iconUrl = it.iconUrl)
            },
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
    OficiosCheckbox(
        checked = state.requiresTurns,
        onCheckedChange = { onEvent(AddServiceEvent.RequiresTurnsChanged(it)) },
        label = stringResource(R.string.services_requires_turns),
        enabled = !state.isEditing || state.canDisableTurns,
    )
    if (state.isEditing && !state.canDisableTurns) {
        Text(
            stringResource(R.string.services_requires_turns_locked),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    OficiosTextField(
        value = state.plazoCancelacionHorasInput,
        onValueChange = { onEvent(AddServiceEvent.PlazoCancelacionHorasChanged(it)) },
        label = stringResource(R.string.services_cancellation_hours),
        supportingText = state.plazoCancelacionHorasError
            ?: stringResource(R.string.services_cancellation_hours_hint),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = state.plazoCancelacionHorasError != null,
    )
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
            isLoading = state.isSearchingBarrios,
            onFocused = { onEvent(AddServiceEvent.LocalBarrioFocused) },
            onSuggestionsDismissed = { onEvent(AddServiceEvent.LocalBarrioSuggestionsDismissed) },
        )
        OficiosTextField(state.localStreet, { onEvent(AddServiceEvent.LocalStreetChanged(it)) }, stringResource(R.string.services_street))
        OficiosTextField(state.localNumber, { onEvent(AddServiceEvent.LocalNumberChanged(it)) }, stringResource(R.string.commercial_local_number))
        OficiosAddressLocationPicker(
            latitude = state.localLocation.latitude,
            longitude = state.localLocation.longitude,
            showMap = state.localLocation.showMap,
            pinDraggable = state.localLocation.pinDraggable,
            showLocateOnMap = state.localLocation.showLocateOnMap,
            showConfirm = state.localLocation.showConfirm,
            confirmed = state.localLocation.confirmed,
            isGeocoding = state.isGeocoding,
            onLocateOnMap = { onEvent(AddServiceEvent.LocateOnMap) },
            onPinChanged = { latitude, longitude ->
                onEvent(AddServiceEvent.LocalMapLocationSelected(latitude, longitude))
            },
            onConfirmPin = { onEvent(AddServiceEvent.ConfirmPin) },
            locateOnMapLabel = state.localLocation.locateOnMapLabel,
            title = "Ubicación del local",
            hint = state.localLocation.message,
        )
    }
}

@Composable
private fun ScheduleStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    if (state.isEditing && state.editorSpecialties.isNotEmpty()) {
        if (state.editorSpecialties.size > 1) {
            Text(stringResource(R.string.services_schedule_pick_specialty), style = MaterialTheme.typography.titleMedium)
            state.editorSpecialties.forEach { option ->
                OficiosFilterChip(
                    label = option.label,
                    selected = option.id == state.editorSourceSubId,
                    onToggle = { onEvent(AddServiceEvent.EditorSpecialtySelected(option.id)) },
                )
            }
        }
        if (state.editorSpecialtyName.isNotBlank()) {
            Text(
                stringResource(R.string.services_schedule_for_specialty, state.editorSpecialtyName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    WeeklyScheduleEditor(
        days = state.weeklyAvailability.toEditorDays(dayOptions().toMap()),
        includeSunday = state.weeklyAvailability.includeSunday,
        onIncludeSundayChange = { onEvent(AddServiceEvent.IncludeSunday(it)) },
        onToggleDay = { day, enabled -> onEvent(AddServiceEvent.WeekDayToggled(day, enabled)) },
        onRangeFromChanged = { day, index, value -> onEvent(AddServiceEvent.WeekRangeFrom(day, index, value)) },
        onRangeToChanged = { day, index, value -> onEvent(AddServiceEvent.WeekRangeTo(day, index, value)) },
        onAddRange = { onEvent(AddServiceEvent.AddWeekRange(it)) },
        onRemoveRange = { day, index -> onEvent(AddServiceEvent.RemoveWeekRange(day, index)) },
    )
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
            val hours = state.weeklyAvailability.days.filter { it.ranges.isNotEmpty() }.joinToString { day ->
                val label = days.firstOrNull { it.first == day.day }?.second ?: day.day
                "$label ${day.ranges.joinToString { "${it.start}–${it.end}" }}"
            }
            Text(
                if (state.isEditing && state.editorSpecialtyName.isNotBlank() && hours.isNotBlank()) {
                    stringResource(R.string.services_specialty_hours, state.editorSpecialtyName, hours)
                } else {
                    hours
                },
            )
            Text(state.paymentMethods.joinToString { id -> payments.firstOrNull { it.first == id }?.second ?: id })
            if (state.servesAtLocal) {
                Text(
                    if (state.localLatitude != null && state.localLongitude != null) stringResource(R.string.services_location_confirmed) else stringResource(R.string.services_location_pending),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    Text(stringResource(R.string.services_review_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun stepTitle(step: AddServiceStep, isEditing: Boolean) = when (step) {
    AddServiceStep.TRADE -> stringResource(
        if (isEditing) R.string.services_edit_trade_title else R.string.services_step_trade_title,
    )
    AddServiceStep.MODALITY -> stringResource(R.string.services_step_modality_title)
    AddServiceStep.SCHEDULE -> stringResource(R.string.services_step_schedule_title)
    AddServiceStep.PAYMENT -> stringResource(R.string.services_step_payment_title)
    AddServiceStep.REVIEW -> stringResource(
        if (isEditing) R.string.services_step_review_title_edit else R.string.services_step_review_title,
    )
}
@Composable
private fun stepDescription(step: AddServiceStep, isEditing: Boolean) = when (step) {
    AddServiceStep.TRADE -> stringResource(
        if (isEditing) R.string.services_edit_trade_hint else R.string.services_step_trade_body,
    )
    AddServiceStep.MODALITY -> stringResource(R.string.services_step_modality_body)
    AddServiceStep.SCHEDULE -> stringResource(
        if (isEditing) R.string.services_step_schedule_body_edit else R.string.services_step_schedule_body,
    )
    AddServiceStep.PAYMENT -> stringResource(R.string.services_step_payment_body)
    AddServiceStep.REVIEW -> stringResource(R.string.services_step_review_body)
}

@Composable
private fun formatWorkSchedule(
    schedules: List<ManagedWorkSchedule>,
    days: List<Pair<String, String>>,
): String {
    val lines = mutableListOf<String>()
    for (schedule in schedules) {
        val from = days.firstOrNull { it.first == schedule.dayFrom }?.second ?: schedule.dayFrom
        val to = days.firstOrNull { it.first == schedule.dayTo }?.second ?: schedule.dayTo
        val label = if (schedule.dayFrom != schedule.dayTo) {
            stringResource(R.string.services_day_range, from, to.lowercase())
        } else {
            from
        }
        lines += "$label ${schedule.ranges.joinToString { "${it.from}–${it.to}" }}"
    }
    return lines.joinToString()
}

private fun WeeklyAvailability.toEditorDays(labels: Map<String, String>): List<WeeklyScheduleDayUi> {
    val keys = if (includeSunday) WeeklyAvailability.ALL_DAYS else WeeklyAvailability.WEEKDAYS
    return keys.map { key ->
        val ranges = rangesOn(key)
        WeeklyScheduleDayUi(
            key = key,
            label = labels[key] ?: key,
            enabled = ranges.isNotEmpty(),
            ranges = ranges.map { WeeklyScheduleRangeUi(it.start, it.end) },
        )
    }
}

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
