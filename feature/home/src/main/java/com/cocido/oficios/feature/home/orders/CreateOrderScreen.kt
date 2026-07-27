package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ClientDomicilio

@Composable
fun CreateOrderScreen(
    uiState: CreateOrderUiState,
    onEvent: (CreateOrderEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(title = "Contratar", onBack = onNavigateBack)
        },
    ) { paddingValues ->
        when (uiState) {
            CreateOrderUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = "Cargando datos…",
            )
            is CreateOrderUiState.Error -> OficiosErrorState(
                title = "No pudimos cargar los datos",
                description = uiState.message,
                onRetry = { onEvent(CreateOrderEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            is CreateOrderUiState.Success -> CreateOrderForm(
                state = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CreateOrderForm(
    state: CreateOrderUiState.Success,
    onEvent: (CreateOrderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = "Solicitar a ${state.workerName}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (state.subRubroOptions.size > 1) {
            OficiosDropdownField(
                value = state.subRubroOptions.firstOrNull { it.id == state.selectedSubRubroId }?.name.orEmpty(),
                options = state.subRubroOptions.map { it.name },
                onOptionSelected = { name ->
                    state.subRubroOptions.firstOrNull { it.name == name }?.let {
                        onEvent(CreateOrderEvent.SubRubroSelected(it.id))
                    }
                },
                label = "Especialidad",
                isError = state.hasAttemptedSubmit && state.selectedSubRubroId == null,
            )
        }

        if (state.domicilios.isEmpty() || state.contactos.isEmpty()) {
            Text(
                text = "Necesitás tener al menos un domicilio y un contacto guardados en tu perfil " +
                    "para poder solicitar un servicio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OficiosDropdownField(
                value = state.domicilios.firstOrNull { it.id == state.selectedDomicilioId }?.let { it.displayLabel() }.orEmpty(),
                options = state.domicilios.map { it.displayLabel() },
                onOptionSelected = { label ->
                    state.domicilios.firstOrNull { it.displayLabel() == label }?.let {
                        onEvent(CreateOrderEvent.DomicilioSelected(it.id))
                    }
                },
                label = "Domicilio",
                isError = state.hasAttemptedSubmit && state.selectedDomicilioId == null,
            )
            OficiosDropdownField(
                value = state.contactos.firstOrNull { it.id == state.selectedContactoId }?.nombre.orEmpty(),
                options = state.contactos.map { it.nombre },
                onOptionSelected = { name ->
                    state.contactos.firstOrNull { it.nombre == name }?.let {
                        onEvent(CreateOrderEvent.ContactoSelected(it.id))
                    }
                },
                label = "Contacto",
                isError = state.hasAttemptedSubmit && state.selectedContactoId == null,
            )
        }

        DateTimePickerButton(
            label = "Elegir fecha y hora",
            selectedDateTime = state.proposedDateTime,
            onDateTimeSelected = { onEvent(CreateOrderEvent.DateTimeSelected(it)) },
        )
        if (state.hasAttemptedSubmit && state.proposedDateTime == null) {
            Text(
                text = "Elegí una fecha y hora propuesta",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        OficiosTextField(
            value = state.description,
            onValueChange = { onEvent(CreateOrderEvent.DescriptionChanged(it)) },
            label = "Descripción (opcional)",
            placeholder = "Contanos qué necesitás",
        )

        OficiosButton(
            text = "Enviar solicitud",
            onClick = { onEvent(CreateOrderEvent.Submit) },
            loading = state.isSubmitting,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun ClientDomicilio.displayLabel(): String =
    listOfNotNull(nombre, listOfNotNull(calle, numero).joinToString(" ").ifBlank { null }, barrioName)
        .joinToString(" · ")
        .ifBlank { "Domicilio #$id" }
