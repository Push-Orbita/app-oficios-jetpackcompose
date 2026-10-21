package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.badges.OficiosVerifiedBadge
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosColorTextSecondaryAccessible
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// WorkerCard — Figma nodo 40:492
//
// Avatar 64 r8 | Nombre + shield-check
//               Número + 5 estrellas (o "Sin calificación")
// Especialidad: texto chico usa token AA (#8EA6CC); Figma #7F9AC5 queda para íconos/grande
// [Contactar Primary]          [Ver ficha completa White]
// ---------------------------------------------------------------------------

@Composable
fun WorkerCard(
    name: String,
    specialty: String,
    rating: Float?,
    avatarUrl: String?,
    isVerified: Boolean = false,
    contactLabel: String,
    viewProfileLabel: String,
    onContact: () -> Unit,
    onViewProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val verifiedCd = stringResource(R.string.ds_worker_verified)
    val ratingCd = if (rating != null) {
        stringResource(R.string.ds_worker_rating, rating)
    } else {
        stringResource(R.string.ds_worker_no_rating)
    }
    OficiosCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OficiosComponentSize.workerCardMinHeight)
            .semantics(mergeDescendants = true) { contentDescription = name },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(OficiosSpacing.md),
        expandContent = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                OficiosAvatar(
                    name = name,
                    imageUrl = avatarUrl,
                    size = OficiosComponentSize.avatarMd,
                    showBorder = true,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = OficiosComponentSize.workerCardTextTopInset),
                    verticalArrangement = Arrangement.spacedBy(OficiosComponentSize.workerCardNameToRating),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xss),
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (isVerified) {
                            OficiosVerifiedBadge(contentDescription = verifiedCd)
                        }
                    }
                    if (rating != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
                            modifier = Modifier.semantics { contentDescription = ratingCd },
                        ) {
                            Text(
                                text = formatWorkerRating(rating),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            repeat(5) { index ->
                                val filled = rating - index
                                val icon = when {
                                    filled >= 1f -> OficiosIcons.Star
                                    filled > 0f -> OficiosIcons.StarHalf
                                    else -> OficiosIcons.Star
                                }
                                val tint = if (filled > 0f) {
                                    OficiosColorAccent
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                }
                                Icon(
                                    imageVector = ImageVector.vectorResource(icon),
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.ds_worker_no_rating),
                            style = MaterialTheme.typography.bodyLarge,
                            color = OficiosColorSecondaryText,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(OficiosComponentSize.workerCardHeaderToSpecialty))
            Text(
                text = specialty,
                style = MaterialTheme.typography.bodyLarge,
                color = OficiosColorTextSecondaryAccessible,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(OficiosComponentSize.workerCardSpecialtyToActions))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OficiosButton(
                    text = contactLabel,
                    onClick = onContact,
                    variant = OficiosButtonVariant.Primary,
                    size = OficiosButtonSize.Small,
                    modifier = Modifier.heightIn(min = OficiosComponentSize.buttonHeightSmall),
                )
                OficiosButton(
                    text = viewProfileLabel,
                    onClick = onViewProfile,
                    variant = OficiosButtonVariant.Secondary,
                    size = OficiosButtonSize.Small,
                    modifier = Modifier.heightIn(min = OficiosComponentSize.buttonHeightSmall),
                )
            }
        }
    }
}

private fun formatWorkerRating(rating: Float): String {
    return if (rating % 1f == 0f) {
        rating.toInt().toString()
    } else {
        String.format("%.1f", rating)
    }
}

@Preview(name = "Worker Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun WorkerCardPreview() {
    OficiosTheme {
        Column(
            modifier = Modifier.padding(OficiosSpacing.md),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            WorkerCard(
                name = "Emanuel Molina",
                specialty = "Revestimiento de cerámicos y azulejos",
                rating = 5f,
                avatarUrl = null,
                isVerified = true,
                contactLabel = "Contactar",
                viewProfileLabel = "Ver ficha completa",
                onContact = {},
                onViewProfile = {},
            )
            WorkerCard(
                name = "Sergio Franco",
                specialty = "Albañilería general",
                rating = null,
                avatarUrl = null,
                isVerified = false,
                contactLabel = "Contactar",
                viewProfileLabel = "Ver ficha completa",
                onContact = {},
                onViewProfile = {},
            )
        }
    }
}
