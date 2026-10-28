package com.cocido.oficios.feature.onboarding.verify

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.inputs.FieldAndCtaBlock
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.motion.oficiosShake
import com.cocido.oficios.core.designsystem.component.motion.rememberOficiosRejectShake
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.steps.OnboardingStepScaffold

@Composable
fun VerifyPhoneStepScreen(
    uiState: VerifyPhoneUiState,
    onCodeChanged: (String) -> Unit,
    onResend: () -> Unit,
    onSubmit: () -> Unit,
    onEditNumber: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shake = rememberOficiosRejectShake(rejectionKey = uiState.codeRejections.takeIf { it > 0 })
    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_verify_phone_title),
        modifier = modifier,
        error = uiState.error,
        isLoading = uiState.isLoading,
        canSubmit = uiState.canSubmit,
        submitText = stringResource(R.string.onboarding_step_verify_phone_submit),
        submitVariant = OficiosButtonVariant.Secondary,
        submitSize = OficiosButtonSize.Large,
        showSubmitButton = false,
        itemSpacing = OficiosSpacing.lg,
        onSubmit = onSubmit,
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_verify_phone_subtitle, uiState.telefono),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (uiState.infoMessage != null) {
            Text(
                text = uiState.infoMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        FieldAndCtaBlock {
            OficiosTextField(
                value = uiState.code,
                onValueChange = onCodeChanged,
                label = stringResource(R.string.onboarding_step_verify_phone_code_label),
                modifier = Modifier.oficiosShake(shake),
                isError = uiState.hasAttemptedSubmit && uiState.codeError != null,
                supportingText = if (uiState.hasAttemptedSubmit) uiState.codeError else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )
            OficiosButton(
                text = when {
                    uiState.isResending -> stringResource(R.string.onboarding_step_verify_phone_resending)
                    uiState.resendCooldownSeconds > 0 -> stringResource(
                        R.string.onboarding_step_verify_phone_resend_cooldown,
                        uiState.resendCooldownSeconds,
                    )
                    else -> stringResource(R.string.onboarding_step_verify_phone_resend)
                },
                onClick = onResend,
                enabled = uiState.resendCooldownSeconds == 0 && !uiState.isResending && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )
            OficiosButton(
                text = stringResource(R.string.onboarding_step_verify_phone_edit_number),
                onClick = onEditNumber,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )
            OficiosButton(
                text = stringResource(R.string.onboarding_step_verify_phone_submit),
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Primary,
                size = OficiosButtonSize.Form,
            )
        }
    }
}
