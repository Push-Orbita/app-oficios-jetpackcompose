package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// BenefitCard — beneficio o descuento para trabajadores afiliados
// ---------------------------------------------------------------------------

@Composable
fun BenefitCard(
    title: String,
    description: String,
    icon: ImageVector = Icons.Outlined.Discount,
    discount: String? = null,
    ctaLabel: String? = null,
    onCtaClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(OficiosComponentSize.iconMd),
            )
            Spacer(Modifier.width(OficiosSpacing.md))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxxs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (discount != null) {
                        Text(
                            text = discount,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OficiosColorMutedText,
                )
                if (ctaLabel != null && onCtaClick != null) {
                    OficiosButton(text = ctaLabel, onClick = onCtaClick)
                }
            }
        }
    }
}

@Preview(name = "Benefit Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun BenefitCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            BenefitCard(
                title = "Ferretería El Clavo",
                description = "10% de descuento en materiales de construcción",
                discount = "10% OFF",
                ctaLabel = "Obtener beneficio",
                onCtaClick = {},
            )
        }
    }
}
