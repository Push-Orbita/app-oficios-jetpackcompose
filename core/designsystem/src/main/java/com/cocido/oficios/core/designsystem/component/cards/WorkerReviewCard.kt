package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// WorkerReviewCard — reseña/feedback de un cliente
// ---------------------------------------------------------------------------

@Composable
fun WorkerReviewCard(
    clientLabel: String?,
    qualityScore: Int?,
    punctualityScore: Int?,
    comment: String?,
    modifier: Modifier = Modifier,
) {
    val starCount = qualityScore
        ?.takeIf { it > 0 }
        ?.coerceIn(0, 10)
        ?.let { score -> ((score.toDouble() / 10.0) * 5.0).roundToInt().coerceIn(0, 5) }

    OficiosCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(top = OficiosSpacing.xs)) {
            if (!clientLabel.isNullOrBlank()) {
                Text(
                    text = clientLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(OficiosSpacing.xxxs))
            }

            if (starCount != null && starCount > 0) {
                Row {
                    repeat(5) { index ->
                        val tint = if (index < starCount) OficiosTheme.colors.accent else MaterialTheme.colorScheme.outlineVariant
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                // Punctuality is shown as text only when available; it shouldn't block the review.
                if (punctualityScore != null) {
                    Text(
                        text = "Puntualidad: $punctualityScore/5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = OficiosSpacing.xxs),
                    )
                }
            } else if (punctualityScore != null) {
                Text(
                    text = "Puntualidad: $punctualityScore/5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!comment.isNullOrBlank()) {
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = OficiosSpacing.xxs),
                )
            }
        }
    }
}

@Preview(name = "Worker Review Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun WorkerReviewCardPreview() {
    OficiosTheme {
        WorkerReviewCard(
            clientLabel = "María G.",
            qualityScore = 9,
            punctualityScore = 4,
            comment = "Excelente trabajo. Puntual y prolijo.",
        )
    }
}

