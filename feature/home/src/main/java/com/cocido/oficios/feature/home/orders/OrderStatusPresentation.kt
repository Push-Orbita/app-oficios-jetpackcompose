package com.cocido.oficios.feature.home.orders

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapHoriz
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorActionContainer
import com.cocido.oficios.core.designsystem.theme.OficiosColorAlertText
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorErrorContainer
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccessContainer
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.feature.home.R

enum class OrderTimelineStep {
    REQUESTED,
    CONFIRMED,
    EN_ROUTE,
    IN_PROGRESS,
    FINISHED,
}

data class OrderStatusVisual(
    val labelRes: Int,
    val explanationRes: Int,
    val icon: ImageVector,
    val container: Color,
    val content: Color,
)

fun orderStatusVisual(status: ServiceRequestStatus, bookingStatus: BookingStatus?): OrderStatusVisual {
    if (status == ServiceRequestStatus.CANCELLED || bookingStatus == BookingStatus.CANCELLED) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_cancelled,
            explanationRes = R.string.orders_explain_cancelled,
            icon = Icons.Outlined.Cancel,
            container = OficiosColorErrorContainer,
            content = OficiosColorAlertText,
        )
    }
    if (status == ServiceRequestStatus.EXPIRED) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_expired,
            explanationRes = R.string.orders_explain_expired,
            icon = Icons.Outlined.Cancel,
            container = OficiosColorErrorContainer,
            content = OficiosColorAlertText,
        )
    }
    if (status == ServiceRequestStatus.COUNTER_OFFER) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_counter,
            explanationRes = R.string.orders_explain_counter,
            icon = Icons.Outlined.SwapHoriz,
            container = OficiosColorAccent,
            content = OficiosColorBackground,
        )
    }
    if (status == ServiceRequestStatus.PENDING || bookingStatus == null || bookingStatus == BookingStatus.PENDING) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_pending,
            explanationRes = R.string.orders_explain_pending,
            icon = Icons.Outlined.HourglassEmpty,
            container = OficiosColorAccent,
            content = OficiosColorBackground,
        )
    }
    return when (bookingStatus) {
        BookingStatus.CONFIRMED -> OrderStatusVisual(
            labelRes = R.string.orders_status_confirmed,
            explanationRes = R.string.orders_explain_confirmed,
            icon = Icons.Outlined.EventAvailable,
            container = OficiosColorAction,
            content = OficiosColorWhite,
        )
        BookingStatus.EN_ROUTE -> OrderStatusVisual(
            labelRes = R.string.orders_status_en_route,
            explanationRes = R.string.orders_explain_en_route,
            icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
            container = OficiosColorActionContainer,
            content = OficiosColorWhite,
        )
        BookingStatus.IN_PROGRESS -> OrderStatusVisual(
            labelRes = R.string.orders_status_in_progress,
            explanationRes = R.string.orders_explain_in_progress,
            icon = Icons.Outlined.Schedule,
            container = OficiosColorSuccessContainer,
            content = OficiosColorWhite,
        )
        BookingStatus.FINISHED -> OrderStatusVisual(
            labelRes = R.string.orders_status_finished,
            explanationRes = R.string.orders_explain_finished,
            icon = Icons.Outlined.CheckCircle,
            container = OficiosColorWhite,
            content = OficiosColorBackground,
        )
        BookingStatus.PENDING, BookingStatus.CANCELLED -> error("cubierto arriba")
    }
}

fun orderTimeline(status: ServiceRequestStatus, bookingStatus: BookingStatus?): List<OrderTimelineStep> {
    if (status == ServiceRequestStatus.CANCELLED || status == ServiceRequestStatus.EXPIRED || bookingStatus == BookingStatus.CANCELLED) {
        return emptyList()
    }
    val includeRoute = bookingStatus == BookingStatus.EN_ROUTE ||
        bookingStatus == BookingStatus.IN_PROGRESS ||
        bookingStatus == BookingStatus.FINISHED
    return buildList {
        add(OrderTimelineStep.REQUESTED)
        add(OrderTimelineStep.CONFIRMED)
        if (includeRoute) add(OrderTimelineStep.EN_ROUTE)
        add(OrderTimelineStep.IN_PROGRESS)
        add(OrderTimelineStep.FINISHED)
    }
}

fun orderTimelineActiveIndex(status: ServiceRequestStatus, bookingStatus: BookingStatus?): Int {
    val steps = orderTimeline(status, bookingStatus)
    if (steps.isEmpty()) return -1
    val current = when {
        status == ServiceRequestStatus.PENDING || status == ServiceRequestStatus.COUNTER_OFFER -> OrderTimelineStep.REQUESTED
        bookingStatus == BookingStatus.EN_ROUTE -> OrderTimelineStep.EN_ROUTE
        bookingStatus == BookingStatus.IN_PROGRESS -> OrderTimelineStep.IN_PROGRESS
        bookingStatus == BookingStatus.FINISHED -> OrderTimelineStep.FINISHED
        else -> OrderTimelineStep.CONFIRMED
    }
    return steps.indexOf(current).coerceAtLeast(0)
}

fun cancelledByLabelRes(cancelledBy: String?): Int? = when (cancelledBy?.uppercase()) {
    "CLIENTE" -> R.string.orders_cancelled_by_client
    "TRABAJADOR" -> R.string.orders_cancelled_by_worker
    "SISTEMA" -> R.string.orders_cancelled_by_system
    else -> null
}
