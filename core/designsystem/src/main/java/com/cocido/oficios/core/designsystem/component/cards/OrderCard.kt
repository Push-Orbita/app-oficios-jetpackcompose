package com.cocido.oficios.core.designsystem.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OrderCard — Figma nodos 198:697 (detalle), 210:29 (versión compacta)
//
// Estructura:
//  ┌─────────────────────────────────────────┐
//  │ [StatusChip]          Monto             │
//  │ Descripción del pedido                  │
//  │ ─────────────────────────────────────── │
//  │ 📍 Dirección                            │
//  │ 👤 Cliente                              │
//  │ 🕐 Horario  💳 Método de pago          │
//  │ ─────────────────────────────────────── │
//  │  [Acción Secundaria]  [Acción Primaria]  │
//  └─────────────────────────────────────────┘
// ---------------------------------------------------------------------------

data class OrderCardAction(val label: String, val onClick: () -> Unit)

@Composable
fun OrderCard(
    detail: String,
    address: String,
    clientName: String,
    scheduledTime: String,
    status: OrderStatus,
    modifier: Modifier = Modifier,
    amount: String? = null,
    paymentMethod: String? = null,
    primaryAction: OrderCardAction? = null,
    secondaryAction: OrderCardAction? = null,
) {
    OficiosCard(modifier = modifier.fillMaxWidth()) {
        // Header: status chip + monto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadgeText(status = status)
            if (amount != null) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.height(OficiosSpacing.sm))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(OficiosSpacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(OficiosSpacing.sm))

        // Metadatos
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
            OrderMetaRow(icon = Icons.Outlined.LocationOn, text = address)
            OrderMetaRow(icon = Icons.Outlined.Person, text = clientName)
            Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
                OrderMetaRow(icon = Icons.Outlined.Schedule, text = scheduledTime, modifier = Modifier.weight(1f))
                if (paymentMethod != null) {
                    OrderMetaRow(icon = Icons.Outlined.AttachMoney, text = paymentMethod, modifier = Modifier.weight(1f))
                }
            }
        }

        // Acciones
        if (primaryAction != null || secondaryAction != null) {
            Spacer(Modifier.height(OficiosSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(OficiosSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                secondaryAction?.let {
                    OficiosButton(
                        text = it.label,
                        onClick = it.onClick,
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Ghost,
                    )
                }
                primaryAction?.let {
                    OficiosButton(
                        text = it.label,
                        onClick = it.onClick,
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeText(status: OrderStatus) {
    Text(
        text = status.label(),
        style = MaterialTheme.typography.labelMedium,
        color = when (status) {
            is OrderStatus.Pending -> MaterialTheme.colorScheme.tertiary
            is OrderStatus.Confirmed -> com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
            is OrderStatus.InReview -> MaterialTheme.colorScheme.onSurfaceVariant
            is OrderStatus.Approved -> com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
            is OrderStatus.Completed -> MaterialTheme.colorScheme.onSurfaceVariant
            is OrderStatus.Rejected -> MaterialTheme.colorScheme.error
            is OrderStatus.Cancelled -> MaterialTheme.colorScheme.error
        },
    )
}

@Composable
private fun OrderMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OficiosColorMutedText,
            modifier = Modifier.size(OficiosComponentSize.iconSm),
        )
        Spacer(modifier = Modifier.padding(start = OficiosSpacing.xxs))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = OficiosColorMutedText)
    }
}

@Preview(name = "Order Card", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OrderCardPreview() {
    OficiosTheme {
        Column(modifier = Modifier.padding(OficiosSpacing.md)) {
            OrderCard(
                detail = "Reparación de cañería rota en baño principal",
                address = "Av. Belgrano 1250, Formosa",
                clientName = "María González",
                scheduledTime = "Hoy, 15:00",
                amount = "$8.500",
                paymentMethod = "Efectivo",
                status = OrderStatus.Confirmed,
                primaryAction = OrderCardAction("Aceptar") {},
                secondaryAction = OrderCardAction("Rechazar") {},
            )
        }
    }
}
