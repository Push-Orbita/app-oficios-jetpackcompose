package com.cocido.oficios.feature.settings.provider

import com.cocido.oficios.feature.settings.R
import androidx.compose.ui.res.stringResource
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
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosContentPhase
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
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
        topBar = { OficiosBackTopBar(title = stringResource(R.string.professional_profile_title), onBack = { onEvent(ProfessionalProfileEvent.Back) }) },
    ) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(padding),
            contentKey = { it.phase() },
        ) { state ->
            when (state.phase()) {
                OficiosContentPhase.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
                OficiosContentPhase.Error -> OficiosErrorState(
                    title = stringResource(R.string.professional_profile_load_error),
                    description = state.errorMessage ?: stringResource(R.string.professional_profile_retry),
                    onRetry = { onEvent(ProfessionalProfileEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                OficiosContentPhase.Empty,
                OficiosContentPhase.Content,
                -> ProfessionalProfileForm(state = state, onEvent = onEvent)
            }
        }
    }
}

private fun ProfessionalProfileUiState.phase(): OficiosContentPhase = when {
    isLoading -> OficiosContentPhase.Loading
    loadFailed -> OficiosContentPhase.Error
    else -> OficiosContentPhase.Content
}

@Composable
private fun ProfessionalProfileForm(
    state: ProfessionalProfileUiState,
    onEvent: (ProfessionalProfileEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg)
            .widthIn(max = OficiosComponentSize.contentMaxWidth),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.professional_profile_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OficiosTextField(
            value = state.description,
            onValueChange = { onEvent(ProfessionalProfileEvent.DescriptionChanged(it)) },
            label = stringResource(R.string.professional_profile_description),
            placeholder = stringResource(R.string.professional_profile_description_hint),
            singleLine = false,
        )
        OficiosTextField(
            value = state.experience,
            onValueChange = { onEvent(ProfessionalProfileEvent.ExperienceChanged(it)) },
            label = stringResource(R.string.professional_profile_experience),
            placeholder = stringResource(R.string.professional_profile_experience_hint),
            singleLine = false,
        )
        Text(
            text = stringResource(R.string.professional_profile_payments),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        PaymentOption(stringResource(R.string.professional_profile_cash), ProviderPaymentMethods.EFECTIVO, state, onEvent)
        PaymentOption(stringResource(R.string.professional_profile_transfer), ProviderPaymentMethods.TRANSFERENCIA, state, onEvent)
        PaymentOption(stringResource(R.string.professional_profile_mercadopago), ProviderPaymentMethods.MERCADOPAGO, state, onEvent)
        OficiosButton(
            text = stringResource(R.string.professional_profile_edit_rubro),
            onClick = { onEvent(ProfessionalProfileEvent.EditRubro) },
            variant = OficiosButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        state.errorMessage?.let { OficiosInlineAlert(it) }
        OficiosButton(
            text = stringResource(R.string.professional_profile_save),
            onClick = { onEvent(ProfessionalProfileEvent.Save) },
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
            loading = state.isSaving,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
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
