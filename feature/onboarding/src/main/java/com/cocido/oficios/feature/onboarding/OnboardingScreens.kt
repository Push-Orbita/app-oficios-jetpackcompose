package com.cocido.oficios.feature.onboarding

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.layout.CenterContent
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.feature.onboarding.common.OnboardingChecklistUiState
import com.cocido.oficios.feature.onboarding.common.hasStepsInReview
import com.cocido.oficios.feature.onboarding.components.DocsWaitingCard
import com.cocido.oficios.feature.onboarding.components.OnboardingStepCard
import com.cocido.oficios.feature.onboarding.navigation.OnboardingStepNavigator
import com.cocido.oficios.feature.onboarding.provider.ProviderStatusUiState

@Composable
fun ClientOnboardingChecklistScreen(
    uiState: OnboardingChecklistUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    OnboardingChecklistScaffold(
        title = stringResource(R.string.onboarding_client_title),
        subtitle = stringResource(R.string.onboarding_client_subtitle),
        uiState = uiState,
        onRetry = onRetry,
        onLogout = onLogout,
        onStepClick = onStepClick,
        isLoggingOut = isLoggingOut,
        modifier = modifier,
    )
}

@Composable
fun ProviderOnboardingChecklistScreen(
    uiState: OnboardingChecklistUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    onFinalizeProfile: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
    isFinalizing: Boolean = false,
    finalizeError: String? = null,
    onDismissBanner: (() -> Unit)? = null,
) {
    OficiosStateContent(targetState = uiState, modifier = modifier) { state ->
        when (state) {
            OnboardingChecklistUiState.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
            is OnboardingChecklistUiState.Error -> OficiosErrorState(
                title = stringResource(R.string.onboarding_provider_error_title),
                description = state.message,
                onRetry = onRetry,
            )
            is OnboardingChecklistUiState.Success -> {
                val showFinalize = state.steps.any {
                    it.id == "finalizar_perfil" && it.status != OnboardingStepStatus.COMPLETE
                }
                val savedWaitingMessage = if (state.savedWaitingForKyc) {
                    stringResource(R.string.onboarding_rubro_pending_kyc)
                } else {
                    null
                }
                ChecklistContent(
                    title = stringResource(R.string.onboarding_provider_title),
                    subtitle = stringResource(R.string.onboarding_provider_subtitle),
                    steps = state.steps.filterNot { showFinalize && it.id == "finalizar_perfil" },
                    onLogout = onLogout,
                    onStepClick = onStepClick,
                    showFinalize = showFinalize,
                    onFinalizeProfile = onFinalizeProfile,
                    isFinalizing = isFinalizing,
                    isLoggingOut = isLoggingOut,
                    showDocsWaiting = state.steps.hasStepsInReview(),
                    extraMessage = finalizeError ?: savedWaitingMessage,
                )
            }
        }
    }
}

@Composable
fun ProviderPendingApprovalScreen(
    uiState: ProviderStatusUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    OficiosStateContent(targetState = uiState, modifier = modifier) { state ->
        when (state) {
            ProviderStatusUiState.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
            is ProviderStatusUiState.Error -> OficiosErrorState(
                title = stringResource(R.string.onboarding_status_error_title),
                description = state.message,
                onRetry = onRetry,
            )
            is ProviderStatusUiState.Pending -> ChecklistContent(
                title = stringResource(R.string.onboarding_pending_title),
                subtitle = stringResource(R.string.onboarding_pending_subtitle),
                steps = state.steps,
                onLogout = onLogout,
                onStepClick = onStepClick,
                isLoggingOut = isLoggingOut,
            )
            is ProviderStatusUiState.Rejected -> Unit
        }
    }
}

@Composable
fun ProviderRejectedScreen(
    uiState: ProviderStatusUiState,
    onRetry: () -> Unit,
    onFixProfile: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    OficiosStateContent(targetState = uiState, modifier = modifier) { state ->
        when (state) {
            ProviderStatusUiState.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
            is ProviderStatusUiState.Error -> OficiosErrorState(
                title = stringResource(R.string.onboarding_status_error_title),
                description = state.message,
                onRetry = onRetry,
            )
            is ProviderStatusUiState.Rejected -> ChecklistContent(
                title = stringResource(R.string.onboarding_rejected_title),
                subtitle = stringResource(R.string.onboarding_rejected_subtitle),
                steps = state.steps,
                extraMessage = state.reason?.let { stringResource(R.string.onboarding_rejected_reason, it) },
                primaryActionLabel = stringResource(R.string.onboarding_fix_profile),
                onPrimaryAction = onFixProfile,
                onLogout = onLogout,
                onStepClick = onStepClick,
                isLoggingOut = isLoggingOut,
            )
            is ProviderStatusUiState.Pending -> Unit
        }
    }
}

@Composable
fun AccountStatusScreen(
    title: String,
    subtitle: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        CenterContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
                OficiosButton(
                    text = stringResource(R.string.onboarding_logout),
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Medium,
                    enabled = !isLoggingOut,
                    loading = isLoggingOut,
                )
            }
        }
    }
}

@Composable
private fun OnboardingChecklistScaffold(
    title: String,
    subtitle: String,
    uiState: OnboardingChecklistUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    OficiosStateContent(targetState = uiState, modifier = modifier) { state ->
        when (state) {
            OnboardingChecklistUiState.Loading -> OficiosLoadingState(modifier = Modifier.fillMaxSize())
            is OnboardingChecklistUiState.Error -> OficiosErrorState(
                title = stringResource(R.string.onboarding_client_error_title),
                description = state.message,
                onRetry = onRetry,
            )
            is OnboardingChecklistUiState.Success -> ChecklistContent(
                title = title,
                subtitle = subtitle,
                steps = state.steps,
                onLogout = onLogout,
                onStepClick = onStepClick,
                isLoggingOut = isLoggingOut,
            )
        }
    }
}

@Composable
private fun ChecklistContent(
    title: String,
    subtitle: String,
    steps: List<OnboardingStep>,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
    extraMessage: String? = null,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    showFinalize: Boolean = false,
    onFinalizeProfile: (() -> Unit)? = null,
    isFinalizing: Boolean = false,
    showDocsWaiting: Boolean = false,
) {
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        CenterContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
                if (showDocsWaiting) {
                    DocsWaitingCard()
                }
                if (extraMessage != null) {
                    Text(text = extraMessage, style = MaterialTheme.typography.bodyMedium)
                }
                steps.forEach { step ->
                    val actionable = OnboardingStepNavigator.isActionable(step)
                    OnboardingStepCard(
                        step = step,
                        statusLabel = when (step.status) {
                            OnboardingStepStatus.COMPLETE -> stringResource(R.string.onboarding_step_complete)
                            OnboardingStepStatus.IN_REVIEW -> stringResource(R.string.onboarding_step_in_review)
                            OnboardingStepStatus.REJECTED -> stringResource(R.string.onboarding_step_rejected)
                            OnboardingStepStatus.BLOCKED -> stringResource(R.string.onboarding_step_blocked)
                            OnboardingStepStatus.PENDING -> stringResource(R.string.onboarding_step_incomplete)
                            OnboardingStepStatus.SAVED -> stringResource(R.string.onboarding_step_saved)
                        },
                        enabled = actionable,
                        onClick = if (actionable) {
                            { onStepClick(step) }
                        } else {
                            null
                        },
                    )
                }
                if (showFinalize && onFinalizeProfile != null) {
                    OficiosButton(
                        text = stringResource(R.string.onboarding_finalize_profile),
                        onClick = onFinalizeProfile,
                        modifier = Modifier.fillMaxWidth(),
                        loading = isFinalizing,
                        enabled = !isFinalizing && !isLoggingOut,
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                    )
                }
                if (primaryActionLabel != null && onPrimaryAction != null) {
                    OficiosButton(
                        text = primaryActionLabel,
                        onClick = onPrimaryAction,
                        modifier = Modifier.fillMaxWidth(),
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                    )
                }
                OficiosButton(
                    text = stringResource(R.string.onboarding_logout),
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Medium,
                    enabled = !isLoggingOut,
                    loading = isLoggingOut,
                )
            }
        }
    }
}
