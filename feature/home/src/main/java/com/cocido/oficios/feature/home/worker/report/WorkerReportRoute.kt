package com.cocido.oficios.feature.home.worker.report

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosInlineAlert
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosDropdownField
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.feature.home.R

@Composable
fun WorkerReportRoute(
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WorkerReportEffect.NavigateBack -> onNavigateBack()
                is WorkerReportEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    WorkerReportScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun WorkerReportScreen(
    uiState: WorkerReportUiState,
    onEvent: (WorkerReportEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.worker_report_title),
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoadingReasons -> OficiosLoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            uiState.reasonsErrorMessage != null && uiState.reasons.isEmpty() -> OficiosErrorState(
                title = stringResource(R.string.worker_report_reasons_error_title),
                description = uiState.reasonsErrorMessage,
                onRetry = { onEvent(WorkerReportEvent.Retry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(OficiosSpacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.worker_report_intro, uiState.workerName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OficiosDropdownField(
                    value = uiState.selectedReasonLabel,
                    options = uiState.reasons.map { it.label },
                    onOptionSelected = { onEvent(WorkerReportEvent.ReasonSelected(it)) },
                    label = stringResource(R.string.worker_report_reason_label),
                    placeholder = stringResource(R.string.worker_report_reason_placeholder),
                    enabled = uiState.reasons.isNotEmpty() && !uiState.isSubmitting,
                )
                OficiosTextField(
                    value = uiState.description,
                    onValueChange = { onEvent(WorkerReportEvent.DescriptionChanged(it)) },
                    label = stringResource(R.string.worker_report_description_label),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(R.string.worker_report_description_placeholder),
                    enabled = !uiState.isSubmitting,
                    supportingText = uiState.descriptionHint,
                    singleLine = false,
                    minLines = 4,
                    maxLines = 8,
                )
                uiState.errorMessage?.let { OficiosInlineAlert(it) }
                OficiosButton(
                    text = stringResource(R.string.worker_report_submit),
                    onClick = { onEvent(WorkerReportEvent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canSubmit,
                    loading = uiState.isSubmitting,
                    variant = OficiosButtonVariant.Danger,
                )
            }
        }
    }
}
