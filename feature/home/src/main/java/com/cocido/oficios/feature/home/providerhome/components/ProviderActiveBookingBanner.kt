package com.cocido.oficios.feature.home.providerhome.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.feature.home.R

// ---------------------------------------------------------------------------
// ProviderActiveBookingBanner — README pantalla 14: banner "AHORA", visible
// solo cuando hay un servicio en curso. Fondo acento dorado (rgb(248,174,26)).
//
// Nota: sin fuente de datos real todavía (el módulo de Pedidos/Booking es
// Fase 3) — este componente queda listo para usarse en cuanto
// ProviderHomeContent.hasActiveBooking refleje un pedido real en curso.
// ---------------------------------------------------------------------------

@Composable
fun ProviderActiveBookingBanner(
    clientName: String,
    serviceTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(OficiosSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.size(OficiosComponentSize.iconMd),
        )
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.provider_home_active_booking_label, serviceTitle, clientName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.background,
            )
        }
    }
}

@Preview(name = "Active Booking Banner", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ProviderActiveBookingBannerPreview() {
    OficiosTheme {
        ProviderActiveBookingBanner(
            clientName = "María González",
            serviceTitle = "Reparación de cañería",
            onClick = {},
        )
    }
}
