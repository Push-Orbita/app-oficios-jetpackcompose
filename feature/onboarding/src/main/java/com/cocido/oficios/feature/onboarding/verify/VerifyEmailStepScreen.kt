package com.cocido.oficios.feature.onboarding.verify

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.inputs.FieldAndCtaBlock
import com.cocido.oficios.core.designsystem.component.inputs.OficiosCodeInput
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.onboarding.R
import com.cocido.oficios.feature.onboarding.steps.OnboardingStepScaffold

@Composable
fun VerifyEmailStepScreen(
    uiState: VerifyEmailUiState,
    onCodeChanged: (String) -> Unit,
    onResend: () -> Unit,
    onSubmit: () -> Unit,
    onEditEmailClick: () -> Unit,
    onDraftEmailChanged: (String) -> Unit,
    onSaveEmail: () -> Unit,
    onCancelEditEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    OnboardingStepScaffold(
        title = stringResource(R.string.onboarding_step_verify_email_title),
        modifier = modifier,
        error = uiState.error,
        isLoading = uiState.isLoading || uiState.isSavingEmail,
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

        if (uiState.isEditingEmail) {
            OficiosTextField(
                value = uiState.draftEmail,
                onValueChange = onDraftEmailChanged,
                label = stringResource(R.string.onboarding_step_verify_email_edit_label),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError
                    ?: stringResource(R.string.onboarding_step_verify_email_edit_hint),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
            ) {
                OficiosButton(
                    text = stringResource(R.string.onboarding_step_verify_email_edit_cancel),
                    onClick = onCancelEditEmail,
                    enabled = !uiState.isSavingEmail,
                    modifier = Modifier.weight(1f),
                    variant = OficiosButtonVariant.Ghost,
                )
                OficiosButton(
                    text = stringResource(R.string.onboarding_step_verify_email_edit_save),
                    onClick = onSaveEmail,
                    enabled = !uiState.isSavingEmail,
                    loading = uiState.isSavingEmail,
                    modifier = Modifier.weight(1f),
                    variant = OficiosButtonVariant.Primary,
                )
            }
        } else {
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            OficiosButton(
                text = stringResource(R.string.onboarding_step_verify_email_wrong_email),
                onClick = onEditEmailClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )
        }

        OficiosButton(
            text = stringResource(R.string.onboarding_step_verify_email_open_email),
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_EMAIL)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(intent) }
            },
            enabled = !uiState.isLoading && !uiState.isEditingEmail,
            modifier = Modifier.fillMaxWidth(),
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )

        Text(
            text = stringResource(R.string.onboarding_step_verify_email_code_sent),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.onboarding_step_verify_email_use_latest),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.onboarding_step_verify_email_spam_hint),
            style = MaterialTheme.typography.bodySmall,
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
            OficiosCodeInput(
                value = uiState.code,
                onValueChange = onCodeChanged,
                isError = uiState.hasAttemptedSubmit && uiState.codeError != null,
                enabled = !uiState.isLoading && !uiState.isEditingEmail,
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
                enabled = uiState.resendCooldownSeconds == 0 &&
                    !uiState.isResending &&
                    !uiState.isLoading &&
                    !uiState.isEditingEmail,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Ghost,
            )

            OficiosButton(
                text = stringResource(R.string.onboarding_step_verify_email_submit),
                onClick = onSubmit,
                enabled = !uiState.isLoading && uiState.canSubmit && !uiState.isEditingEmail,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Primary,
                size = OficiosButtonSize.Form,
            )
        }
    }
}
