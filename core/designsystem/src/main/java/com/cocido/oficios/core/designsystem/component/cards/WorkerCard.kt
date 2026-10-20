package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// WorkerCard — Figma nodo 40:309
//
// Estructura actualizada según Figma:
//  ┌──────────────────────────────────────────┐
//  │ Avatar 64 r8  Nombre + shield-check 24   │
//  │               Número + ★★★★★ (5 estrellas│
//  │               o "Sin Calificación")      │
//  │               especialidad #7F9AC5       │
//  │  [Contactar Primary]  [Ver ficha White] │
//  └──────────────────────────────────────────┘
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
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            OficiosAvatar(
                name = name,
                imageUrl = avatarUrl,
                size = OficiosComponentSize.avatarMd,
            )

            Spacer(Modifier.width(OficiosSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                // Nombre + verificado (shield-check)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isVerified) {
                        Spacer(Modifier.width(OficiosSpacing.xxs))
                        Icon(
                            imageVector = ImageVector.vectorResource(OficiosIcons.ShieldCheck),
                            contentDescription = "Verificado",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                // Rating - Número + 5 estrellas (Figma 40:309)
                Spacer(Modifier.height(OficiosSpacing.xxxs))
                if (rating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xxxs)
                    ) {
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        repeat(5) { index ->
                            val icon = if (index < rating.toInt()) {
                                if (rating - index >= 1f) OficiosIcons.Star
                                else OficiosIcons.StarHalf
                            } else {
                                OficiosIcons.Star
                            }
                            Icon(
                                imageVector = ImageVector.vectorResource(icon),
                                contentDescription = null,
                                tint = if (index < rating) OficiosColorAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Sin Calificación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OficiosColorSecondaryText,
                    )
                }

                // Especialidad en #7F9AC5 (OficiosColorSecondaryText)
                Spacer(Modifier.height(OficiosSpacing.xxxs))
                Text(
                    text = specialty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OficiosColorSecondaryText,
                )
            }
        }

        Spacer(Modifier.height(OficiosSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OficiosButton(
                text = contactLabel,
                onClick = onContact,
                variant = OficiosButtonVariant.Primary,
                size = OficiosButtonSize.Small,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(OficiosSpacing.xs))
            OficiosButton(
                text = viewProfileLabel,
                onClick = onViewProfile,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Small,
                modifier = Modifier.weight(1f),
            )
        }
            OficiosButton(
                text = contactLabel,
                onClick = onContact,
                modifier = Modifier.weight(1f),
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Medium,
            )
            OficiosButton(
                text = viewProfileLabel,
                onClick = onViewProfile,
                modifier = Modifier.weight(1f),
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Medium,
            )
        }
    }
}

@Preview(name = "Worker Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun WorkerCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            WorkerCard(
                name = "Carlos Rodríguez",
                specialty = "Plomero · 5 años de experiencia",
                rating = 4.8f,
                avatarUrl = null,
                isVerified = true,
                contactLabel = "Contactar",
                viewProfileLabel = "Ver perfil",
                onContact = {},
                onViewProfile = {},
            )
        }
    }
}
