package com.cocido.oficios.feature.onboarding.verify

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.inputs.FieldAndCtaBlock
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCodeInput
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.steps.OnboardingStepScaffold

@Composable
fun VerifyEmailStepScreen(
    uiState: VerifyEmailUiState,
    onCodeChanged: (String) -> Unit,
    onResend: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_verify_email_title),
        modifier = modifier,
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        submitText = stringResource(R.string.onboarding_step_verify_email_submit),
        submitVariant = OficiosButtonVariant.Secondary,
        submitSize = OficiosButtonSize.Large,
        showSubmitButton = false,
        itemSpacing = OficiosSpacing.lg,
        onSubmit = onSubmit,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_verify_email_subtitle_no_format),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        
        // Email en negrita
        Text(
            text = uiState.email,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        
        // Botón "Abrir correo"
        OficiosButton(
            text = stringResource(R.string.onboarding_step_verify_email_open_email),
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_EMAIL)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Si no hay app de correo, no hacer nada
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Primary,
            size = OficiosButtonSize.Large,
        )
        
        if (uiState.infoMessage != null) {
            Text(
                text = uiState.infoMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.onboarding_step_verify_email_spam_hint),
                style = MaterialTheme.typography.bodySmall,
                color = OficiosColorMutedText,
            )
        }
        
        FieldAndCtaBlock {
            // Componente de 6 dígitos
            OficiosCodeInput(
                value = uiState.code,
                onValueChange = onCodeChanged,
                isError = uiState.hasAttemptedSubmit && uiState.codeError != null,
                enabled = !uiState.isLoading,
                onDone = onSubmit,
            )
            
            if (uiState.hasAttemptedSubmit && uiState.codeError != null) {
                Text(
                    text = uiState.codeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            
            OficiosButton(
                text = when {
                    uiState.isResending -> stringResource(R.string.onboarding_step_verify_email_resending)
                    uiState.resendCooldownSeconds > 0 -> stringResource(
                        R.string.onboarding_step_verify_email_resend_cooldown,
                        uiState.resendCooldownSeconds,
                    )
                    else -> stringResource(R.string.onboarding_step_verify_email_resend)
                },
                onClick = onResend,
                enabled = uiState.resendCooldownSeconds == 0 && !uiState.isResending && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )
            
            OficiosButton(
                text = stringResource(R.string.onboarding_step_verify_email_submit),
                onClick = onSubmit,
                enabled = !uiState.isLoading && uiState.canSubmit,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
            )
        }
    }
}
