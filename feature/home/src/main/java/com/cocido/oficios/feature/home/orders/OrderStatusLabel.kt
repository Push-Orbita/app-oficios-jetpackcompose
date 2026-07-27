package com.cocido.oficios.feature.home.orders

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.theme.OficiosColorAlert
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus

fun statusDisplayLabel(status: ServiceRequestStatus, bookingStatus: BookingStatus?): String = when (status) {
    ServiceRequestStatus.PENDING -> "Pendiente"
    ServiceRequestStatus.COUNTER_OFFER -> "Contrapropuesta"
    ServiceRequestStatus.ACCEPTED -> when (bookingStatus) {
        BookingStatus.FINISHED -> "Finalizado"
        BookingStatus.EN_ROUTE -> "En camino"
        BookingStatus.IN_PROGRESS -> "En curso"
        else -> "Confirmado"
    }
    ServiceRequestStatus.CANCELLED -> "Cancelado"
    ServiceRequestStatus.EXPIRED -> "Expirado"
}

@Composable
fun ServiceOrderStatusLabel(
    status: ServiceRequestStatus,
    bookingStatus: BookingStatus?,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        ServiceRequestStatus.PENDING, ServiceRequestStatus.COUNTER_OFFER -> MaterialTheme.colorScheme.tertiary
        ServiceRequestStatus.ACCEPTED -> when (bookingStatus) {
            BookingStatus.FINISHED -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.primary
        }
        ServiceRequestStatus.CANCELLED, ServiceRequestStatus.EXPIRED -> OficiosColorAlert
    }
    Text(
        text = statusDisplayLabel(status, bookingStatus),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier,
    )
}
