package com.cocido.oficios.feature.settings.provider

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
        topBar = { OficiosBackTopBar("Servicios que ofrecés", onBack = { onEvent(ServicesEvent.Back) }) },
    ) { padding ->
        when {
            uiState.isLoading && uiState.services.isEmpty() -> OficiosLoadingState(Modifier.fillMaxSize().padding(padding))
            uiState.errorMessage != null && uiState.services.isEmpty() -> OficiosErrorState(
                title = "No pudimos cargar tus servicios",
                description = uiState.errorMessage,
                onRetry = { onEvent(ServicesEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = OficiosSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                item {
                    Text(
                        "Cada servicio agrupa sus especialidades, modalidad y disponibilidad. Sumá solo lo que realmente ofrecés para recibir pedidos relevantes.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                uiState.errorMessage?.let { message -> item { OficiosInlineAlert(message) } }
                if (uiState.services.isEmpty()) {
                    item {
                        OficiosEmptyState(
                            title = "Todavía no publicaste servicios",
                            description = "Agregá tu primer oficio para empezar a aparecer en búsquedas.",
                            ctaLabel = "Agregar servicio",
                            onCta = { onEvent(ServicesEvent.AddService) },
                        )
                    }
                } else {
                    items(uiState.services, key = { it.id }) { service ->
                        ServiceCard(service, uiState.isDeleting, onEvent)
                    }
                    item {
                        OficiosButton(
                            text = "Agregar otro servicio",
                            leadingIcon = Icons.Outlined.Add,
                            onClick = { onEvent(ServicesEvent.AddService) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item { Column(Modifier.padding(bottom = OficiosSpacing.xl)) {} }
            }
        }
    }
    uiState.pendingDelete?.let { service ->
        OficiosDialog(
            title = "Eliminar ${service.rubroName}",
            message = "Dejarás de aparecer en búsquedas de este servicio. Los pedidos históricos no se eliminan.",
            confirmLabel = "Eliminar servicio",
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
                if (service.isPrimary) Text("Servicio principal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                Text(
                    service.subServices.joinToString { it.name }.ifBlank { "Sin especialidades informadas" },
                    style = MaterialTheme.typography.bodyMedium,
                )
                val modalities = buildList {
                    if (service.subServices.any { it.servesAtClientHome }) add("A domicilio")
                    if (service.subServices.any { it.servesAtLocal }) add("En local")
                    if (service.subServices.any { it.requiresTurns }) add("Con turnos")
                }
                Text(modalities.joinToString(" · ").ifBlank { "Modalidad sin informar" }, style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
                if (service.workSchedule.isNotEmpty()) {
                    Text(
                        service.workSchedule.joinToString { schedule ->
                            "${dayLabel(schedule.dayFrom)}${if (schedule.dayFrom != schedule.dayTo) " a ${dayLabel(schedule.dayTo).lowercase()}" else ""} ${schedule.ranges.joinToString { "${it.from}–${it.to}" }}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OficiosColorSecondaryText,
                    )
                }
                if (service.isPrimary) {
                    Text("El servicio principal se conserva para proteger tu perfil y tus pedidos activos.", style = MaterialTheme.typography.labelSmall, color = OficiosColorSecondaryText)
                }
            }
            if (!service.isPrimary) {
                IconButton(enabled = !disabled, onClick = { onEvent(ServicesEvent.RequestDelete(service)) }) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar ${service.rubroName}")
                }
            }
        }
    }
}

@Composable
fun AddServiceScreen(uiState: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    OficiosScaffold(
        topBar = { OficiosBackTopBar("Agregar servicio", onBack = { onEvent(AddServiceEvent.Previous) }) },
    ) { padding ->
        if (uiState.isLoading && uiState.categories.isEmpty()) {
            OficiosLoadingState(Modifier.fillMaxSize().padding(padding))
        } else if (uiState.errorMessage != null && uiState.categories.isEmpty()) {
            OficiosErrorState(
                title = "No pudimos preparar el formulario",
                description = uiState.errorMessage,
                onRetry = { onEvent(AddServiceEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(OficiosSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                Text("Paso ${uiState.step.ordinal + 1} de ${AddServiceStep.entries.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
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
                        OficiosButton("Atrás", { onEvent(AddServiceEvent.Previous) }, Modifier.weight(1f), OficiosButtonVariant.Outlined)
                    }
                    OficiosButton(
                        text = if (uiState.step == AddServiceStep.REVIEW) "Publicar servicio" else "Continuar",
                        onClick = { onEvent(if (uiState.step == AddServiceStep.REVIEW) AddServiceEvent.Save else AddServiceEvent.Next) },
                        modifier = Modifier.weight(1f),
                        loading = uiState.isSaving,
                        enabled = !uiState.isSaving,
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
        label = "Categoría",
        placeholder = "Elegí una categoría",
    )
    OficiosDropdownField(
        value = state.selectedRubroLabel,
        options = state.rubros.map { it.label },
        onOptionSelected = { label -> state.rubros.firstOrNull { it.label == label }?.let { onEvent(AddServiceEvent.RubroSelected(it.id)) } },
        label = "Oficio",
        placeholder = if (state.selectedCategoryId == null) "Primero elegí una categoría" else "Elegí un oficio",
        enabled = state.selectedCategoryId != null && !state.isLoading,
    )
    if (state.subRubros.isNotEmpty()) {
        Text("Especialidades", style = MaterialTheme.typography.titleMedium)
        state.subRubros.forEach { option ->
            OficiosCheckbox(option.id in state.selectedSubRubroIds, { onEvent(AddServiceEvent.SubRubroToggled(option.id, it)) }, option.label)
        }
    }
}

@Composable
private fun ModalityStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    OficiosCheckbox(state.servesAtClientHome, { onEvent(AddServiceEvent.ClientHomeChanged(it)) }, "Voy al domicilio del cliente")
    OficiosCheckbox(state.servesAtLocal, { onEvent(AddServiceEvent.LocalChanged(it)) }, "Atiendo en mi local")
    OficiosCheckbox(state.requiresTurns, { onEvent(AddServiceEvent.RequiresTurnsChanged(it)) }, "Este servicio requiere turno")
    if (state.servesAtLocal) {
        Text("Dirección del local", style = MaterialTheme.typography.titleMedium)
        OficiosTextField(state.localName, { onEvent(AddServiceEvent.LocalNameChanged(it)) }, "Nombre del local (opcional)", placeholder = "Ej: Taller Centro")
        OficiosAutocompleteField(
            value = state.localBarrioQuery,
            onValueChange = { onEvent(AddServiceEvent.LocalBarrioChanged(it)) },
            suggestions = state.localBarrioSuggestions,
            onSuggestionSelected = { onEvent(AddServiceEvent.LocalBarrioSelected(it)) },
            suggestionLabel = { it.label },
            label = "Barrio",
            placeholder = "Buscá y seleccioná el barrio",
        )
        OficiosTextField(state.localStreet, { onEvent(AddServiceEvent.LocalStreetChanged(it)) }, "Calle y altura")
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
    Text("Días habituales", style = MaterialTheme.typography.titleMedium)
    DAYS.forEach { (id, label) -> OficiosCheckbox(id in state.workDays, { onEvent(AddServiceEvent.WorkDayToggled(id, it)) }, label, compactRow = true) }
    Text("Horarios", style = MaterialTheme.typography.titleMedium)
    state.timeSlots.forEachIndexed { index, slot ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            OficiosDropdownField(slot.from, TIME_OPTIONS, { onEvent(AddServiceEvent.TimeFromChanged(index, it)) }, "Desde", Modifier.weight(1f))
            OficiosDropdownField(slot.to, TIME_OPTIONS, { onEvent(AddServiceEvent.TimeToChanged(index, it)) }, "Hasta", Modifier.weight(1f))
            if (state.timeSlots.size > 1) IconButton(onClick = { onEvent(AddServiceEvent.RemoveTimeSlot(index)) }) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Quitar horario")
            }
        }
    }
    OficiosButton("Agregar franja horaria", { onEvent(AddServiceEvent.AddTimeSlot) }, Modifier.fillMaxWidth(), OficiosButtonVariant.Outlined, leadingIcon = Icons.Outlined.Add)
}

@Composable
private fun PaymentStep(state: AddServiceUiState, onEvent: (AddServiceEvent) -> Unit) {
    paymentOptions.forEach { (id, label) -> OficiosCheckbox(id in state.paymentMethods, { onEvent(AddServiceEvent.PaymentToggled(id, it)) }, label) }
}

@Composable
private fun ReviewStep(state: AddServiceUiState) {
    OficiosCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Text(state.selectedRubroLabel, style = MaterialTheme.typography.titleLarge)
            Text(state.subRubros.filter { it.id in state.selectedSubRubroIds }.joinToString { it.label })
            Text(buildList { if (state.servesAtClientHome) add("A domicilio"); if (state.servesAtLocal) add("En local"); if (state.requiresTurns) add("Con turnos") }.joinToString(" · "))
            Text(state.workDays.map(::dayLabel).joinToString())
            Text(state.timeSlots.joinToString { "${it.from}–${it.to}" })
            Text(state.paymentMethods.map(::paymentLabel).joinToString())
            if (state.servesAtLocal) {
                Text(
                    if (state.localLatitude != null && state.localLongitude != null) "Ubicación del local confirmada" else "Ubicación del local pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = OficiosColorSecondaryText,
                )
            }
        }
    }
    Text("Revisá todo antes de publicar. Después vas a poder gestionar los turnos desde Agenda.", style = MaterialTheme.typography.bodySmall, color = OficiosColorSecondaryText)
}

private fun stepTitle(step: AddServiceStep) = when (step) {
    AddServiceStep.TRADE -> "¿Qué servicio ofrecés?"
    AddServiceStep.MODALITY -> "¿Dónde y cómo atendés?"
    AddServiceStep.SCHEDULE -> "¿Cuándo trabajás?"
    AddServiceStep.PAYMENT -> "¿Cómo cobrás?"
    AddServiceStep.REVIEW -> "Revisá tu nuevo servicio"
}
private fun stepDescription(step: AddServiceStep) = when (step) {
    AddServiceStep.TRADE -> "Elegí el oficio y todas las especialidades que realmente realizás."
    AddServiceStep.MODALITY -> "Podés ir al domicilio del cliente, atender en un local o usar ambas modalidades."
    AddServiceStep.SCHEDULE -> "Definí una disponibilidad habitual; los turnos concretos se administran desde Agenda."
    AddServiceStep.PAYMENT -> "Mostrá de antemano qué medios de cobro aceptás."
    AddServiceStep.REVIEW -> "Esta información será visible para los clientes."
}

private val DAYS = listOf("LUNES" to "Lunes", "MARTES" to "Martes", "MIERCOLES" to "Miércoles", "JUEVES" to "Jueves", "VIERNES" to "Viernes", "SABADO" to "Sábado", "DOMINGO" to "Domingo")
private val TIME_OPTIONS = buildList { for (hour in 0..23) for (minute in listOf(0, 30)) add("%02d:%02d".format(hour, minute)) }
private val paymentOptions = listOf(ProviderPaymentMethods.EFECTIVO to "Efectivo", ProviderPaymentMethods.TRANSFERENCIA to "Transferencia bancaria", ProviderPaymentMethods.MERCADOPAGO to "Mercado Pago")
private fun paymentLabel(value: String) = paymentOptions.firstOrNull { it.first == value }?.second ?: value
private fun dayLabel(value: String) = DAYS.firstOrNull { it.first == value }?.second ?: value.lowercase().replaceFirstChar { it.uppercase() }
