package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosRadius
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// ProfileCompletionCard — indicador de completitud de perfil del trabajador
// ---------------------------------------------------------------------------

@Composable
fun ProfileCompletionCard(
    completionPercent: Int,
    onCompleteProfile: () -> Unit,
    modifier: Modifier = Modifier,
    missingItems: List<String> = emptyList(),
) {
    if (completionPercent >= 100) return

    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Completá tu perfil",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$completionPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            LinearProgressIndicator(
                progress = { completionPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(OficiosSpacing.xxs)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
            )

            if (missingItems.isNotEmpty()) {
                Text(
                    text = "Falta: ${missingItems.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OficiosColorMutedText,
                )
            }

            Spacer(Modifier.height(OficiosSpacing.xxs))
            OficiosButton(
                text = "Completar perfil",
                onClick = onCompleteProfile,
                modifier = Modifier.fillMaxWidth(),
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Medium,
            )
        }
    }
}

@Preview(name = "Profile Completion Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProfileCompletionCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            ProfileCompletionCard(
                completionPercent = 65,
                onCompleteProfile = {},
                missingItems = listOf("Foto de perfil", "Certificaciones"),
            )
        }
    }
}
