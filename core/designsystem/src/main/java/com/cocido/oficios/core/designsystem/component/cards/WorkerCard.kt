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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosColorTertiary
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// WorkerCard — Figma nodo 40:309
//
// Estructura:
//  ┌──────────────────────────────┐
//  │ Avatar 64dp  Nombre + shield │
//  │              ★★★★★ 4.8       │
//  │              descripción muted│
//  │  [Contactar]  [Ver perfil]   │
//  └──────────────────────────────┘
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
                // Nombre + verificado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isVerified) {
                        Spacer(Modifier.width(OficiosSpacing.xxs))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verificado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(OficiosComponentSize.iconSm),
                        )
                    }
                }

                // Rating
                if (rating != null) {
                    Spacer(Modifier.height(OficiosSpacing.xxxs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = OficiosColorTertiary,
                            modifier = Modifier.size(OficiosComponentSize.iconSm),
                        )
                        Spacer(Modifier.width(OficiosSpacing.xxxs))
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = OficiosColorTertiary,
                        )
                    }
                }

                // Especialidad
                Spacer(Modifier.height(OficiosSpacing.xxxs))
                Text(
                    text = specialty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OficiosColorMutedText,
                )
            }
        }

        Spacer(Modifier.height(OficiosSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            OficiosButton(
                text = contactLabel,
                onClick = onContact,
                modifier = Modifier.weight(1f),
            )
            OficiosButton(
                text = viewProfileLabel,
                onClick = onViewProfile,
                modifier = Modifier.weight(1f),
                variant = OficiosButtonVariant.Secondary,
            )
        }
    }
}

@Preview(name = "Worker Card", showBackground = true, backgroundColor = 0xFF231D38)
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
