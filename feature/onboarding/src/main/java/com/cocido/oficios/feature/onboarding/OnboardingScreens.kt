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
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.layout.CenterContent
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus
import com.cocido.oficios.feature.onboarding.client.ClientOnboardingUiState
import com.cocido.oficios.feature.onboarding.components.OnboardingStepCard
import com.cocido.oficios.feature.onboarding.navigation.OnboardingStepNavigator
import com.cocido.oficios.feature.onboarding.provider.ProviderOnboardingUiState
import com.cocido.oficios.feature.onboarding.provider.ProviderStatusUiState

@Composable
fun ClientOnboardingChecklistScreen(
    uiState: ClientOnboardingUiState,
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
    uiState: ProviderOnboardingUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    onFinalizeProfile: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
    isFinalizing: Boolean = false,
) {
    when (uiState) {
        ProviderOnboardingUiState.Loading -> OficiosLoadingState(modifier = modifier.fillMaxSize())
        is ProviderOnboardingUiState.Error -> OficiosErrorState(
            title = "Error",
            description = uiState.message,
            onRetry = onRetry,
        )
        is ProviderOnboardingUiState.Success -> ChecklistContent(
            title = stringResource(R.string.onboarding_provider_title),
            subtitle = stringResource(R.string.onboarding_provider_subtitle),
            steps = uiState.steps,
            onLogout = onLogout,
            onStepClick = onStepClick,
            showFinalize = uiState.steps.any {
                it.id == "finalizar_perfil" && it.status != OnboardingStepStatus.COMPLETE
            },
            onFinalizeProfile = onFinalizeProfile,
            isFinalizing = isFinalizing,
            isLoggingOut = isLoggingOut,
            modifier = modifier,
        )
    }
}

@Composable
fun ProviderPendingApprovalScreen(
    uiState: ProviderStatusUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    when (uiState) {
        ProviderStatusUiState.Loading -> OficiosLoadingState(modifier = modifier.fillMaxSize())
        is ProviderStatusUiState.Error -> OficiosErrorState(
            title = "Error",
            description = uiState.message,
            onRetry = onRetry,
        )
        is ProviderStatusUiState.Pending -> ChecklistContent(
            title = stringResource(R.string.onboarding_pending_title),
            subtitle = stringResource(R.string.onboarding_pending_subtitle),
            steps = uiState.steps,
            onLogout = onLogout,
            onStepClick = {},
            isLoggingOut = isLoggingOut,
            modifier = modifier,
        )
        is ProviderStatusUiState.Rejected -> Unit
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
    when (uiState) {
        ProviderStatusUiState.Loading -> OficiosLoadingState(modifier = modifier.fillMaxSize())
        is ProviderStatusUiState.Error -> OficiosErrorState(
            title = "Error",
            description = uiState.message,
            onRetry = onRetry,
        )
        is ProviderStatusUiState.Rejected -> ChecklistContent(
            title = stringResource(R.string.onboarding_rejected_title),
            subtitle = stringResource(R.string.onboarding_rejected_subtitle),
            steps = uiState.steps,
            extraMessage = uiState.reason?.let { stringResource(R.string.onboarding_rejected_reason, it) },
            primaryActionLabel = stringResource(R.string.onboarding_fix_profile),
            onPrimaryAction = onFixProfile,
            onLogout = onLogout,
            onStepClick = onStepClick,
            isLoggingOut = isLoggingOut,
            modifier = modifier,
        )
        is ProviderStatusUiState.Pending -> Unit
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
                    enabled = !isLoggingOut,
                    loading = isLoggingOut,
                )
            }
        }
    }
}

@Composable
fun HomeShellScreen(
    title: String,
    subtitle: String,
    onContinue: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        CenterContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
                OficiosButton(
                    text = stringResource(R.string.onboarding_go_home_client),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                )
                OficiosButton(
                    text = stringResource(R.string.onboarding_logout),
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
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
    uiState: ClientOnboardingUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onStepClick: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    isLoggingOut: Boolean = false,
) {
    when (uiState) {
        ClientOnboardingUiState.Loading -> OficiosLoadingState(modifier = modifier.fillMaxSize())
        is ClientOnboardingUiState.Error -> OficiosErrorState(
            title = "Error",
            description = uiState.message,
            onRetry = onRetry,
        )
        is ClientOnboardingUiState.Success -> ChecklistContent(
            title = title,
            subtitle = subtitle,
            steps = uiState.steps,
            onLogout = onLogout,
            onStepClick = onStepClick,
            isLoggingOut = isLoggingOut,
            modifier = modifier,
        )
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
                if (extraMessage != null) {
                    Text(text = extraMessage, style = MaterialTheme.typography.bodyMedium)
                }
                steps.forEach { step ->
                    val actionable = OnboardingStepNavigator.isActionable(step)
                    OnboardingStepCard(
                        step = step,
                        statusLabel = when (step.status) {
                            OnboardingStepStatus.COMPLETE -> stringResource(R.string.onboarding_step_complete)
                            else -> stringResource(R.string.onboarding_step_pending)
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
                    )
                }
                if (primaryActionLabel != null && onPrimaryAction != null) {
                    OficiosButton(
                        text = primaryActionLabel,
                        onClick = onPrimaryAction,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OficiosButton(
                    text = stringResource(R.string.onboarding_logout),
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    enabled = !isLoggingOut,
                    loading = isLoggingOut,
                )
            }
        }
    }
}
