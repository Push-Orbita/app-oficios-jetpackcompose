package com.cocido.oficios.feature.home.orders

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
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSuccessState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
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
        topBar = { OficiosBackTopBar(title = "Calificar", onBack = onNavigateBack) },
    ) { paddingValues ->
        when (uiState) {
            is RateWorkerUiState.Form -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
            ) {
                Text(
                    text = "¿Cómo estuvo el servicio de ${uiState.workerName}?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                    Text(text = "Calidad del trabajo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    StarSelector(selected = uiState.qualityStars, onSelect = { onEvent(RateWorkerEvent.QualitySelected(it)) })
                }
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                    Text(text = "Puntualidad", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    StarSelector(selected = uiState.punctualityStars, onSelect = { onEvent(RateWorkerEvent.PunctualitySelected(it)) })
                }
                if (uiState.hasAttemptedSubmit && (uiState.qualityStars == 0 || uiState.punctualityStars == 0)) {
                    Text(
                        text = "Elegí una calificación para calidad y puntualidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                OficiosTextField(
                    value = uiState.comment,
                    onValueChange = { onEvent(RateWorkerEvent.CommentChanged(it)) },
                    label = "Reseña (opcional)",
                    placeholder = "Contanos tu experiencia",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onEvent(RateWorkerEvent.Submit) }),
                )
                OficiosButton(
                    text = "Enviar",
                    onClick = { onEvent(RateWorkerEvent.Submit) },
                    loading = uiState.isSubmitting,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Prominent,
                )
            }
            is RateWorkerUiState.Submitted -> OficiosSuccessState(
                title = "Enviado ✓",
                description = "Gracias por calificar a ${uiState.workerName}.",
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                ctaLabel = "Volver",
                onCta = onNavigateBack,
            )
        }
    }
}

@Composable
private fun StarSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
    maxStars: Int = 5,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
        for (star in 1..maxStars) {
            Icon(
                imageVector = if (star <= selected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (star == 1) "1 estrella" else "$star estrellas",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onSelect(star) },
            )
        }
    }
}
