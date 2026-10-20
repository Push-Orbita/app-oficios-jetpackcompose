package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.feature.home.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosAnimatedSuccessOverlay
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.feedback.rememberOficiosHaptics
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.motion.oficiosBounce
import com.cocido.oficios.core.designsystem.component.motion.rememberOficiosBounce
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun RateWorkerScreen(
    uiState: RateWorkerUiState,
    onEvent: (RateWorkerEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = stringResource(R.string.rate_worker_title), onBack = onNavigateBack) },
    ) { paddingValues ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues),
        ) { state ->
            when (state) {
                is RateWorkerUiState.Form -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
                ) {
                    Text(
                        text = stringResource(R.string.rate_worker_question, state.workerName),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                        Text(text = stringResource(R.string.rate_worker_quality), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        StarSelector(selected = state.qualityStars, onSelect = { onEvent(RateWorkerEvent.QualitySelected(it)) })
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                        Text(text = stringResource(R.string.rate_worker_punctuality), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        StarSelector(selected = state.punctualityStars, onSelect = { onEvent(RateWorkerEvent.PunctualitySelected(it)) })
                    }
                    if (state.hasAttemptedSubmit && (state.qualityStars == 0 || state.punctualityStars == 0)) {
                        Text(
                            text = stringResource(R.string.rate_worker_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    OficiosTextField(
                        value = state.comment,
                        onValueChange = { onEvent(RateWorkerEvent.CommentChanged(it)) },
                        label = stringResource(R.string.rate_worker_review),
                        placeholder = stringResource(R.string.rate_worker_review_hint),
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onEvent(RateWorkerEvent.Submit) }),
                    )
                    OficiosButton(
                        text = stringResource(R.string.rate_worker_submit),
                        onClick = { onEvent(RateWorkerEvent.Submit) },
                        loading = state.isSubmitting,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                    )
                }
                is RateWorkerUiState.Submitted -> OficiosAnimatedSuccessOverlay(
                    title = stringResource(R.string.rate_worker_sent),
                    description = stringResource(R.string.rate_worker_thanks, state.workerName),
                    action = {
                        OficiosButton(
                            text = stringResource(R.string.rate_worker_back),
                            onClick = onNavigateBack,
                            variant = OficiosButtonVariant.Secondary,
                            size = OficiosButtonSize.Large,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun StarSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
    maxStars: Int = 5,
) {
    val haptics = rememberOficiosHaptics()
    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
        for (star in 1..maxStars) {
            val bounce = rememberOficiosBounce(selectionKey = star.takeIf { it == selected })
            Icon(
                imageVector = if (star <= selected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (star == 1) {
                    stringResource(R.string.rate_worker_star)
                } else {
                    stringResource(R.string.rate_worker_stars, star)
                },
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(OficiosComponentSize.ratingIcon)
                    .oficiosBounce(bounce)
                    .clickable {
                        haptics.tick()
                        onSelect(star)
                    },
            )
        }
    }
}
