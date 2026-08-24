package com.cocido.oficios.feature.home.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.calendar.OficiosScheduleSlot
import com.cocido.oficios.core.designsystem.component.calendar.ScheduleSlotData
import com.cocido.oficios.core.designsystem.component.calendar.ScheduleSlotState
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import com.cocido.oficios.feature.home.orders.DateTimePickerButton
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AgendaListScreen(
    uiState: AgendaListUiState,
    onEvent: (AgendaListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = "Agenda",
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEvent(AgendaListEvent.OpenConfig) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Configurar agenda",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when (uiState) {
            AgendaListUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = "Cargando agenda…",
            )
            is AgendaListUiState.Error -> OficiosErrorState(
                title = "No pudimos cargar tu agenda",
                description = uiState.message,
                onRetry = { onEvent(AgendaListEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            is AgendaListUiState.Success -> AgendaListContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun AgendaListContent(
    uiState: AgendaListUiState.Success,
    onEvent: (AgendaListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.subRubroOptions.isEmpty()) {
        OficiosEmptyState(
            title = "Todavía no tenés especialidades con agenda",
            description = "Configurá tus rubros y sub-rubros para poder armar tu agenda de turnos.",
            icon = Icons.Outlined.EventBusy,
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        if (uiState.subRubroOptions.size > 1) {
            item {
                OficiosDropdownField(
                    value = uiState.selectedSubRubro?.subRubroLabel.orEmpty(),
                    options = uiState.subRubroOptions.map { it.subRubroLabel },
                    onOptionSelected = { label ->
                        uiState.subRubroOptions.firstOrNull { it.subRubroLabel == label }?.let {
                            onEvent(AgendaListEvent.SubRubroSelected(it.id))
                        }
                    },
                    label = "Especialidad",
                )
            }
        }

        item {
            if (uiState.showCreateSlotForm) {
                CreateSlotForm(uiState = uiState, onEvent = onEvent)
            } else {
                OficiosButton(
                    text = "+ Nuevo turno",
                    onClick = { onEvent(AgendaListEvent.OpenCreateSlot) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (uiState.isLoadingSlots) {
            item {
                OficiosLoadingState(message = "Cargando turnos…")
            }
        } else if (uiState.slots.isEmpty()) {
            item {
                OficiosEmptyState(
                    title = "Todavía no tenés turnos cargados",
                    description = "Creá un turno para que los clientes puedan reservarlo.",
                    icon = Icons.Outlined.CalendarMonth,
                )
            }
        } else {
            items(uiState.slots, key = { it.id }) { slot ->
                AgendaSlotRow(slot = slot, onDeleteClick = { onEvent(AgendaListEvent.RequestDeleteSlot(slot.id)) })
            }
        }
    }

    if (uiState.slotPendingDeleteId != null) {
        OficiosDialog(
            title = "Eliminar turno",
            message = "¿Querés eliminar este turno de tu agenda? Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = { onEvent(AgendaListEvent.ConfirmDeleteSlot) },
            onDismiss = { onEvent(AgendaListEvent.DismissDeleteSlot) },
            isDangerous = true,
        )
    }
}

@Composable
private fun CreateSlotForm(
    uiState: AgendaListUiState.Success,
    onEvent: (AgendaListEvent) -> Unit,
) {
    OficiosCard {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Text(text = "Nuevo turno", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            DateTimePickerButton(
                label = "Inicio",
                selectedDateTime = uiState.newSlotStart,
                onDateTimeSelected = { onEvent(AgendaListEvent.NewSlotStartSelected(it)) },
            )
            DateTimePickerButton(
                label = "Fin",
                selectedDateTime = uiState.newSlotEnd,
                onDateTimeSelected = { onEvent(AgendaListEvent.NewSlotEndSelected(it)) },
            )
            OficiosCheckbox(
                checked = uiState.newSlotAutoAcceptance,
                onCheckedChange = { onEvent(AgendaListEvent.NewSlotAutoAcceptanceChanged(it)) },
                label = "Aceptar reservas automáticamente",
            )
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                OficiosButton(
                    text = "Guardar turno",
                    onClick = { onEvent(AgendaListEvent.SubmitNewSlot) },
                    enabled = uiState.canSubmitNewSlot && !uiState.isSavingSlot,
                    loading = uiState.isSavingSlot,
                    modifier = Modifier.fillMaxWidth(),
                )
                OficiosButton(
                    text = "Cancelar",
                    onClick = { onEvent(AgendaListEvent.DismissCreateSlot) },
                    variant = OficiosButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AgendaSlotRow(slot: AgendaSlot, onDeleteClick: () -> Unit) {
    val dateLabel = slot.start.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-AR")))
        .replaceFirstChar { it.uppercase() }
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = dateLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar turno",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        OficiosScheduleSlot(
            slot = ScheduleSlotData(
                startTime = slot.start.format(DateTimeFormatter.ofPattern("HH:mm")),
                endTime = slot.end.format(DateTimeFormatter.ofPattern("HH:mm")),
                serviceTitle = slot.subRubroLabel,
                state = when (slot.status) {
                    AgendaSlotStatus.DISPONIBLE -> ScheduleSlotState.Available
                    AgendaSlotStatus.RESERVADO -> ScheduleSlotState.Booked
                    AgendaSlotStatus.BLOQUEADO -> ScheduleSlotState.Blocked
                },
            ),
        )
    }
}
