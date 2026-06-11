package com.cocido.oficios.feature.onboarding.steps

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCheckbox
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.file.resolveUriToFilePath
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.client.ClientContactoUiState
import com.cocido.oficios.feature.onboarding.client.ClientDomicilioUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderDniUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderLocalUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderProfessionalInfoUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderRubrosUiState

@Composable
fun OnboardingStepScaffold(
    title: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isLoading: Boolean = false,
    canSubmit: Boolean = true,
    onSubmit: () -> Unit,
    content: @Composable () -> Unit,
) {
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(OficiosSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            if (error != null) OficiosInlineAlert(message = error)
            content()
            OficiosButton(
                text = stringResource(R.string.onboarding_step_save),
                onClick = onSubmit,
                enabled = canSubmit && !isLoading,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ClientDomicilioStepScreen(
    uiState: ClientDomicilioUiState,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioSelected: (com.cocido.oficios.domain.model.CatalogOption) -> Unit,
    onCalleChanged: (String) -> Unit,
    onNumeroChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_domicilio_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        OficiosTextField(
            value = uiState.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            label = stringResource(R.string.onboarding_step_barrio_label),
            placeholder = stringResource(R.string.onboarding_step_barrio_hint),
        )
        uiState.barrioOptions.forEach { option ->
            OficiosButton(
                text = option.label,
                onClick = { onBarrioSelected(option) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OficiosTextField(
            value = uiState.calle,
            onValueChange = onCalleChanged,
            label = stringResource(R.string.onboarding_step_calle_label),
        )
        OficiosTextField(
            value = uiState.numero,
            onValueChange = onNumeroChanged,
            label = stringResource(R.string.onboarding_step_numero_label),
        )
    }
}

@Composable
fun ClientContactoStepScreen(
    uiState: ClientContactoUiState,
    onNombreChanged: (String) -> Unit,
    onTelefonoChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_contacto_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        OficiosTextField(value = uiState.nombre, onValueChange = onNombreChanged, label = "Nombre")
        OficiosTextField(value = uiState.telefono, onValueChange = onTelefonoChanged, label = "WhatsApp")
        OficiosTextField(value = uiState.email, onValueChange = onEmailChanged, label = "Email (opcional)")
    }
}

@Composable
fun ProviderDniStepScreen(
    uiState: ProviderDniUiState,
    onDocumentoChanged: (String) -> Unit,
    onFrontImage: (String?) -> Unit,
    onBackImage: (String?) -> Unit,
    onSubmitNumber: () -> Unit,
    onSubmitImages: () -> Unit,
) {
    val context = LocalContext.current
    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        onFrontImage(uri?.let { resolveUriToFilePath(context, it) })
    }
    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        onBackImage(uri?.let { resolveUriToFilePath(context, it) })
    }
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_dni_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.documento.isNotBlank(),
        onSubmit = onSubmitNumber,
    ) {
        OficiosTextField(value = uiState.documento, onValueChange = onDocumentoChanged, label = "Número de DNI")
        OficiosButton(
            text = if (uiState.frontImagePath != null) "Frente cargado" else "Subir frente del DNI",
            onClick = { frontLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosButton(
            text = if (uiState.backImagePath != null) "Dorso cargado" else "Subir dorso del DNI",
            onClick = { backLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
        )
        OficiosButton(
            text = stringResource(R.string.onboarding_step_upload_dni),
            onClick = onSubmitImages,
            enabled = uiState.frontImagePath != null && uiState.backImagePath != null && !uiState.isLoading,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ProviderProfessionalInfoStepScreen(
    uiState: ProviderProfessionalInfoUiState,
    onDescripcionChanged: (String) -> Unit,
    onExperienciaChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_professional_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.descripcion.isNotBlank() && uiState.experiencia.isNotBlank(),
        onSubmit = onSubmit,
    ) {
        OficiosTextField(value = uiState.descripcion, onValueChange = onDescripcionChanged, label = "Descripción")
        OficiosTextField(value = uiState.experiencia, onValueChange = onExperienciaChanged, label = "Experiencia")
    }
}

@Composable
fun ProviderRubrosStepScreen(
    uiState: ProviderRubrosUiState,
    onCategorySelected: (Long) -> Unit,
    onTradeSelected: (Long) -> Unit,
    onSubRubroToggled: (Long, Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_rubros_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        onSubmit = onSubmit,
    ) {
        OficiosDropdownField(
            value = uiState.categoryOptions.find { it.id == uiState.selectedCategoryId }?.label.orEmpty(),
            options = uiState.categoryOptions.map { it.label },
            onOptionSelected = { label ->
                uiState.categoryOptions.find { it.label == label }?.let { onCategorySelected(it.id) }
            },
            label = "Categoría",
            placeholder = "Seleccionar",
            usePlaceholderOnly = true,
        )
        OficiosDropdownField(
            value = uiState.tradeOptions.find { it.id == uiState.selectedTradeId }?.label.orEmpty(),
            options = uiState.tradeOptions.map { it.label },
            onOptionSelected = { label ->
                uiState.tradeOptions.find { it.label == label }?.let { onTradeSelected(it.id) }
            },
            label = "Rubro",
            placeholder = "Seleccionar",
            usePlaceholderOnly = true,
        )
        uiState.specializationOptions.forEach { option ->
            OficiosCheckbox(
                checked = option.id in uiState.selectedSubRubroIds,
                onCheckedChange = { onSubRubroToggled(option.id, it) },
                label = option.label,
                compactRow = true,
            )
        }
    }
}

@Composable
fun ProviderLocalStepScreen(
    uiState: ProviderLocalUiState,
    onBarrioQueryChanged: (String) -> Unit,
    onBarrioSelected: (com.cocido.oficios.domain.model.CatalogOption) -> Unit,
    onCalleChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_local_title),
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.selectedBarrioId != null && uiState.calle.isNotBlank(),
        onSubmit = onSubmit,
    ) {
        OficiosTextField(
            value = uiState.barrioQuery,
            onValueChange = onBarrioQueryChanged,
            label = stringResource(R.string.onboarding_step_barrio_label),
        )
        uiState.barrioOptions.forEach { option ->
            OficiosButton(text = option.label, onClick = { onBarrioSelected(option) }, modifier = Modifier.fillMaxWidth())
        }
        OficiosTextField(value = uiState.calle, onValueChange = onCalleChanged, label = stringResource(R.string.onboarding_step_calle_label))
    }
}
