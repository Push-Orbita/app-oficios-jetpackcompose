package com.cocido.oficios.feature.auth.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.inputs.OficiosAutocompleteField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPhoneField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphSubtitleStyle
import com.cocido.oficios.core.designsystem.theme.OficiosAuthGlyphTitleStyle
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.CatalogOption
import com.cocido.oficios.domain.model.RegistrationRole
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.feature.auth.components.AuthRegisterLayout
import com.cocido.oficios.feature.auth.register.RegisterFormScreen
import com.cocido.oficios.feature.auth.register.RegisterMode

@Composable
fun RegistrationWizardPersonalScreen(
    state: RegistrationWizardUiState,
    onEvent: (com.cocido.oficios.feature.auth.register.RegisterFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    RegisterFormScreen(
        mode = if (state.role == RegistrationRole.CLIENT) RegisterMode.CLIENT else RegisterMode.PROVIDER,
        uiState = state.personal,
        onEvent = onEvent,
        modifier = modifier,
        submitLabel = stringResource(R.string.auth_next_button),
    )
}

@Composable
fun RegistrationWizardProfileScreen(
    state: RegistrationWizardUiState,
    onContactNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioFocused: () -> Unit,
    onBarrioSelected: (CatalogOption) -> Unit,
    onBarrioDismissed: () -> Unit,
    onCalleChanged: (String) -> Unit,
    onNumeroChanged: (String) -> Unit,
    onPisoChanged: (String) -> Unit,
    onDepartamentoChanged: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthRegisterLayout(modifier = modifier, sectionSpacing = OficiosSpacing.md) {
        Text(
            text = stringResource(R.string.auth_wizard_profile_title),
            style = OficiosAuthGlyphTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.auth_wizard_profile_subtitle),
            style = OficiosAuthGlyphSubtitleStyle,
        )
        state.profileError?.let { OficiosInlineAlert(it, modifier = Modifier.fillMaxWidth()) }
        OficiosTextField(
            value = state.contactName,
            onValueChange = onContactNameChanged,
            label = stringResource(R.string.auth_wizard_contact_name),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosPhoneField(
            value = state.phone,
            onValueChange = onPhoneChanged,
            selectedCountry = state.selectedCountry,
            label = stringResource(R.string.auth_wizard_whatsapp),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosAutocompleteField(
            value = state.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            suggestions = state.barrioOptions,
            onSuggestionSelected = onBarrioSelected,
            suggestionLabel = { it.label },
            label = stringResource(R.string.auth_wizard_barrio),
            isLoading = state.isSearchingBarrios,
            onFocused = onBarrioFocused,
            onSuggestionsDismissed = onBarrioDismissed,
        )
        OficiosTextField(
            value = state.calle,
            onValueChange = onCalleChanged,
            label = stringResource(R.string.auth_wizard_street),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosTextField(
            value = state.numero,
            onValueChange = onNumeroChanged,
            label = stringResource(R.string.auth_wizard_number),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosTextField(
            value = state.piso,
            onValueChange = onPisoChanged,
            label = stringResource(R.string.auth_wizard_floor),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosTextField(
            value = state.departamento,
            onValueChange = onDepartamentoChanged,
            label = stringResource(R.string.auth_wizard_apartment),
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosButton(
            text = stringResource(R.string.auth_next_button),
            onClick = onContinue,
            enabled = !state.isCommitting,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Primary,
            size = OficiosButtonSize.Form,
        )
    }
}

@Composable
fun RegistrationWizardReviewScreen(
    state: RegistrationWizardUiState,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthRegisterLayout(modifier = modifier, isLoading = state.isCommitting, sectionSpacing = OficiosSpacing.md) {
        Text(
            text = stringResource(R.string.auth_wizard_review_title),
            style = OficiosAuthGlyphTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.auth_wizard_review_subtitle),
            style = OficiosAuthGlyphSubtitleStyle,
        )
        state.commitError?.let { OficiosInlineAlert(it, modifier = Modifier.fillMaxWidth()) }
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            ReviewLine(stringResource(R.string.auth_first_name_label), state.personal.firstName)
            ReviewLine(stringResource(R.string.auth_last_name_label), state.personal.lastName)
            ReviewLine(stringResource(R.string.auth_email_electronic_label), state.personal.email)
            ReviewLine(stringResource(R.string.auth_wizard_whatsapp), state.phone)
            ReviewLine(stringResource(R.string.auth_wizard_barrio), state.barrioQuery)
            ReviewLine(
                stringResource(R.string.auth_wizard_street),
                listOf(state.calle, state.numero).filter { it.isNotBlank() }.joinToString(" "),
            )
        }
        Spacer(Modifier.height(OficiosSpacing.md))
        OficiosButton(
            text = stringResource(R.string.auth_create_account),
            onClick = onCreateAccount,
            enabled = !state.isCommitting,
            loading = state.isCommitting,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Primary,
            size = OficiosButtonSize.Form,
        )
    }
}

@Composable
private fun ReviewLine(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge)
    }
}
