package com.cocido.oficios.feature.settings.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ProviderPaymentMethods

@Composable
fun ProfessionalProfileScreen(
    uiState: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvent) -> Unit,
) {
    OficiosScaffold(
        topBar = { OficiosBackTopBar(title = "Presentación profesional", onBack = { onEvent(ProfessionalProfileEvent.Back) }) },
    ) { padding ->
        when {
            uiState.isLoading -> OficiosLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            uiState.loadFailed -> OficiosErrorState(
                title = "No pudimos cargar tu perfil profesional",
                description = uiState.errorMessage ?: "Intentá nuevamente.",
                onRetry = { onEvent(ProfessionalProfileEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(OficiosSpacing.lg)
                    .widthIn(max = OficiosComponentSize.contentMaxWidth),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                Text(
                    text = "Explicá con claridad qué hacés y qué experiencia tenés. Es lo primero que evalúa un cliente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OficiosTextField(
                    value = uiState.description,
                    onValueChange = { onEvent(ProfessionalProfileEvent.DescriptionChanged(it)) },
                    label = "Descripción profesional",
                    placeholder = "Ej: Instalaciones, reparaciones y mantenimiento…",
                    singleLine = false,
                )
                OficiosTextField(
                    value = uiState.experience,
                    onValueChange = { onEvent(ProfessionalProfileEvent.ExperienceChanged(it)) },
                    label = "Experiencia",
                    placeholder = "Ej: Más de 5 años trabajando en…",
                    singleLine = false,
                )
                Text(
                    text = "Medios de cobro",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                PaymentOption("Efectivo", ProviderPaymentMethods.EFECTIVO, uiState, onEvent)
                PaymentOption("Transferencia bancaria", ProviderPaymentMethods.TRANSFERENCIA, uiState, onEvent)
                PaymentOption("Mercado Pago", ProviderPaymentMethods.MERCADOPAGO, uiState, onEvent)
                uiState.errorMessage?.let { OficiosInlineAlert(it) }
                OficiosButton(
                    text = "Guardar cambios",
                    onClick = { onEvent(ProfessionalProfileEvent.Save) },
                    loading = uiState.isSaving,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PaymentOption(
    label: String,
    method: String,
    uiState: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvent) -> Unit,
) {
    OficiosCheckbox(
        checked = method in uiState.paymentMethods,
        onCheckedChange = { onEvent(ProfessionalProfileEvent.PaymentToggled(method, it)) },
        label = label,
    )
}
