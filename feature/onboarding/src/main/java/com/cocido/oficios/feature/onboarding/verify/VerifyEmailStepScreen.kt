package com.cocido.oficios.feature.onboarding.verify

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
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
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_verify_email_title),
        modifier = modifier,
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        submitText = stringResource(R.string.onboarding_step_verify_email_submit),
        submitVariant = OficiosButtonVariant.Secondary,
        submitSize = OficiosButtonSize.Prominent,
        onSubmit = onSubmit,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_verify_email_subtitle, uiState.email),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OficiosTextField(
            value = uiState.code,
            onValueChange = onCodeChanged,
            label = stringResource(R.string.onboarding_step_verify_email_code_label),
            isError = uiState.hasAttemptedSubmit && uiState.codeError != null,
            supportingText = if (uiState.hasAttemptedSubmit) uiState.codeError else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
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
    }
}
