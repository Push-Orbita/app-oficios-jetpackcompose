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
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// CertificateCard — certificación/habilitación del trabajador
// ---------------------------------------------------------------------------

@Composable
fun CertificateCard(
    title: String,
    issuedBy: String? = null,
    expiryDate: String? = null,
    isVerified: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
                Spacer(Modifier.width(OficiosSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxxs)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (issuedBy != null) {
                        Text(
                            text = issuedBy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (expiryDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(OficiosComponentSize.iconSm),
                            )
                            Spacer(Modifier.width(OficiosSpacing.xxxs))
                            Text(
                                text = "Vence: $expiryDate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (isVerified) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verificado",
                    tint = OficiosTheme.colors.success,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
        }
    }
}

@Preview(name = "Certificate Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun CertificateCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            CertificateCard(
                title = "Electricista Matriculado",
                issuedBy = "Municipalidad de Formosa",
                expiryDate = "dic 2026",
                isVerified = true,
            )
        }
    }
}
