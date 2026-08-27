package com.cocido.oficios.feature.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosPasswordField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onEvent: (ChangePasswordEvent) -> Unit,
) {
    OficiosScaffold(
        topBar = { OficiosBackTopBar(title = "Cambiar contraseña", onBack = { onEvent(ChangePasswordEvent.Back) }) },
    ) { padding ->
        when {
            uiState.isLoading -> OficiosLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            uiState.email.isBlank() && uiState.errorMessage != null -> OficiosErrorState(
                title = "No pudimos validar tu cuenta",
                description = uiState.errorMessage,
                onRetry = { onEvent(ChangePasswordEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(OficiosSpacing.lg)
                    .widthIn(max = OficiosComponentSize.contentMaxWidth),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            ) {
                Text(
                    text = "Usá una contraseña que no repitas en otros servicios. Debe tener al menos 6 caracteres.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OficiosPasswordField(
                    value = uiState.password,
                    onValueChange = { onEvent(ChangePasswordEvent.PasswordChanged(it)) },
                    label = "Nueva contraseña",
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError,
                )
                OficiosPasswordField(
                    value = uiState.confirmation,
                    onValueChange = { onEvent(ChangePasswordEvent.ConfirmationChanged(it)) },
                    label = "Repetir nueva contraseña",
                    isError = uiState.confirmationError != null,
                    supportingText = uiState.confirmationError,
                )
                uiState.errorMessage?.let { OficiosInlineAlert(it) }
                OficiosButton(
                    text = "Actualizar contraseña",
                    onClick = { onEvent(ChangePasswordEvent.Submit) },
                    loading = uiState.isSubmitting,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
