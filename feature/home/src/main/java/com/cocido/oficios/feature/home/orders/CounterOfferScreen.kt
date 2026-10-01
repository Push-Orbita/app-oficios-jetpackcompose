package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun CounterOfferScreen(
    uiState: CounterOfferUiState,
    onEvent: (CounterOfferEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = "Reagendar pedido", onBack = onNavigateBack) },
    ) { paddingValues ->
        if (uiState is CounterOfferUiState.Form) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                Text(
                    text = "Proponele al cliente una nueva fecha y hora para el servicio.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DateTimePickerButton(
                    label = "Elegir nueva fecha y hora",
                    selectedDateTime = uiState.dateTime,
                    onDateTimeSelected = { onEvent(CounterOfferEvent.DateTimeSelected(it)) },
                )
                if (uiState.hasAttemptedSubmit && uiState.dateTime == null) {
                    Text(
                        text = "Elegí una nueva fecha y hora",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                OficiosTextField(
                    value = uiState.price,
                    onValueChange = { onEvent(CounterOfferEvent.PriceChanged(it)) },
                    label = "Nuevo precio (opcional)",
                    placeholder = "Ej: 15000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OficiosButton(
                    text = "Enviar contrapropuesta",
                    onClick = { onEvent(CounterOfferEvent.Submit) },
                    loading = uiState.isSubmitting,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Prominent,
                )
            }
        }
    }
}
