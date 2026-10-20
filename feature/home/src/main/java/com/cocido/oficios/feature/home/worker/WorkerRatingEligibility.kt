package com.cocido.oficios.feature.home.worker

import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceOrderListItem

fun pendingRatingBookingId(
    workerId: Long,
    orders: List<ServiceOrderListItem>,
    ratedBookingIds: Set<Long>,
): Long? = orders.firstOrNull { order ->
    val bookingId = order.bookingId
    order.workerId == workerId &&
        order.bookingStatus == BookingStatus.FINISHED &&
        bookingId != null &&
        bookingId !in ratedBookingIds
}?.bookingId
