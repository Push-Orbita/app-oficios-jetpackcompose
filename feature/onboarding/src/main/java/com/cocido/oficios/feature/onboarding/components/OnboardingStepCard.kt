package com.cocido.oficios.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus

@Composable
fun OnboardingStepCard(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
    statusLabel: String,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OficiosColorMutedText,
                )
            }
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = when (step.status) {
                    OnboardingStepStatus.COMPLETE -> MaterialTheme.colorScheme.primary
                    OnboardingStepStatus.REJECTED -> MaterialTheme.colorScheme.error
                    OnboardingStepStatus.BLOCKED -> OficiosColorMutedText
                    OnboardingStepStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                },
            )
        }
    }
}
