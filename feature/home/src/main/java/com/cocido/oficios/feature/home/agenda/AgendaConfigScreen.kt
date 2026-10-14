package com.cocido.oficios.feature.home.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosEmptyState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.AgendaSessionConfig

@Composable
fun AgendaConfigScreen(
    uiState: AgendaConfigUiState,
    onEvent: (AgendaConfigEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = "Configurar agenda", onBack = onNavigateBack) },
    ) { paddingValues ->
        when (uiState) {
            AgendaConfigUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = "Cargando configuración…",
            )
            is AgendaConfigUiState.Error -> OficiosErrorState(
                title = "No pudimos cargar tu configuración",
                description = uiState.message,
                onRetry = { onEvent(AgendaConfigEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            is AgendaConfigUiState.Success -> AgendaConfigContent(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}

@Composable
private fun AgendaConfigContent(
    uiState: AgendaConfigUiState.Success,
    onEvent: (AgendaConfigEvent) -> Unit,
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
                            onEvent(AgendaConfigEvent.SubRubroSelected(it.id))
                        }
                    },
                    label = "Especialidad",
                )
            }
        }

        item {
            Text(
                text = "Duración e intervalo de turnos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        item {
            OficiosCard {
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                    OficiosTextField(
                        value = uiState.durationInput,
                        onValueChange = { onEvent(AgendaConfigEvent.DurationChanged(it)) },
                        label = "Duración del turno (minutos)",
                        isError = uiState.durationError != null,
                        supportingText = uiState.durationError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OficiosTextField(
                        value = uiState.intervalInput,
                        onValueChange = { onEvent(AgendaConfigEvent.IntervalChanged(it)) },
                        label = "Intervalo entre turnos (minutos, opcional)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OficiosButton(
                        text = "Guardar",
                        onClick = { onEvent(AgendaConfigEvent.Save) },
                        enabled = !uiState.isSaving,
                        loading = uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                    )
                }
            }
        }

        if (uiState.sessions.isNotEmpty()) {
            item {
                Text(
                    text = "Configuraciones guardadas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(uiState.sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    onDeleteClick = { onEvent(AgendaConfigEvent.RequestDeleteSession(session.id)) },
                )
            }
        }
    }

    if (uiState.sessionPendingDeleteId != null) {
        OficiosDialog(
            title = "Eliminar configuración",
            message = "¿Querés eliminar esta duración de turnos? Los horarios ya creados no se modificarán.",
            confirmLabel = "Eliminar",
            confirmEnabled = !uiState.isDeleting,
            onConfirm = { onEvent(AgendaConfigEvent.ConfirmDeleteSession) },
            onDismiss = { onEvent(AgendaConfigEvent.DismissDeleteSession) },
            isDangerous = true,
        )
    }
}

@Composable
private fun SessionRow(session: AgendaSessionConfig, onDeleteClick: () -> Unit) {
    OficiosCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "${session.durationMinutes} min por turno",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                session.intervalMinutes?.let {
                    Text(
                        text = "Intervalo: $it min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar configuración",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
