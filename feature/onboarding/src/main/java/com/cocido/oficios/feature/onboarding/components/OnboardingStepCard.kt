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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.clickable
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.theme.OficiosColorError
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosColorWarning
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.OnboardingStep
import com.cocido.oficios.domain.model.OnboardingStepStatus

@Composable
fun OnboardingStepCard(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
    statusLabel: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    OficiosCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled && onClick != null) {
                    Modifier
                        .semantics(mergeDescendants = true) { contentDescription = "${step.title}. $statusLabel" }
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
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
                    OnboardingStepStatus.COMPLETE -> OficiosColorSuccess
                    OnboardingStepStatus.IN_REVIEW -> OficiosColorWarning
                    OnboardingStepStatus.REJECTED -> OficiosColorError
                    OnboardingStepStatus.BLOCKED -> OficiosColorMutedText
                    OnboardingStepStatus.PENDING -> OficiosColorError
                },
            )
        }
    }
}
