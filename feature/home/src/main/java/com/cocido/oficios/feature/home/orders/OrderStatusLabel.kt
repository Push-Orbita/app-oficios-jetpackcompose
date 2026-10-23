package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.theme.DarkOficiosColors
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus

fun statusDisplayLabel(status: ServiceRequestStatus, bookingStatus: BookingStatus?): Int =
    orderStatusVisual(status, bookingStatus, DarkOficiosColors).labelRes

@Composable
fun ServiceOrderStatusLabel(
    status: ServiceRequestStatus,
    bookingStatus: BookingStatus?,
    modifier: Modifier = Modifier,
) {
    val visual = orderStatusVisual(status, bookingStatus)
    OficiosChip(
        label = stringResource(visual.labelRes),
        modifier = modifier,
        containerColor = visual.container,
        contentColor = visual.content,
        borderColor = visual.container,
        leadingIcon = visual.icon,
    )
}
