package com.cocido.oficios.feature.home.orders

import androidx.compose.runtime.Composable
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
import com.cocido.oficios.core.designsystem.theme.OficiosColors
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
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

@Composable
fun orderStatusVisual(
    status: ServiceRequestStatus,
    bookingStatus: BookingStatus?,
): OrderStatusVisual = orderStatusVisual(status, bookingStatus, OficiosTheme.colors)

fun orderStatusVisual(
    status: ServiceRequestStatus,
    bookingStatus: BookingStatus?,
    colors: OficiosColors,
): OrderStatusVisual {
    if (status == ServiceRequestStatus.CANCELLED || bookingStatus == BookingStatus.CANCELLED) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_cancelled,
            explanationRes = R.string.orders_explain_cancelled,
            icon = Icons.Outlined.Cancel,
            container = colors.errorContainer,
            content = colors.onError,
        )
    }
    if (status == ServiceRequestStatus.EXPIRED || bookingStatus == BookingStatus.EXPIRED) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_expired,
            explanationRes = R.string.orders_explain_expired,
            icon = Icons.Outlined.Cancel,
            container = colors.errorContainer,
            content = colors.onError,
        )
    }
    if (status == ServiceRequestStatus.COUNTER_OFFER) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_counter,
            explanationRes = R.string.orders_explain_counter,
            icon = Icons.Outlined.SwapHoriz,
            container = colors.accent,
            content = colors.onWarning,
        )
    }
    if (status == ServiceRequestStatus.PENDING || bookingStatus == null || bookingStatus == BookingStatus.PENDING) {
        return OrderStatusVisual(
            labelRes = R.string.orders_status_pending,
            explanationRes = R.string.orders_explain_pending,
            icon = Icons.Outlined.HourglassEmpty,
            container = colors.accent,
            content = colors.onWarning,
        )
    }
    return when (bookingStatus) {
        BookingStatus.CONFIRMED -> OrderStatusVisual(
            labelRes = R.string.orders_status_confirmed,
            explanationRes = R.string.orders_explain_confirmed,
            icon = Icons.Outlined.EventAvailable,
            container = colors.primary,
            content = colors.onPrimary,
        )
        BookingStatus.EN_ROUTE -> OrderStatusVisual(
            labelRes = R.string.orders_status_en_route,
            explanationRes = R.string.orders_explain_en_route,
            icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
        )
        BookingStatus.IN_PROGRESS -> OrderStatusVisual(
            labelRes = R.string.orders_status_in_progress,
            explanationRes = R.string.orders_explain_in_progress,
            icon = Icons.Outlined.Schedule,
            container = colors.successContainer,
            content = colors.onSuccess,
        )
        BookingStatus.FINISHED -> OrderStatusVisual(
            labelRes = R.string.orders_status_finished,
            explanationRes = R.string.orders_explain_finished,
            icon = Icons.Outlined.CheckCircle,
            container = colors.inverseSurface,
            content = colors.inverseOnSurface,
        )
        BookingStatus.PENDING, BookingStatus.CANCELLED, BookingStatus.EXPIRED -> error("cubierto arriba")
    }
}

fun orderTimelineStepIcon(step: OrderTimelineStep): ImageVector = when (step) {
    OrderTimelineStep.REQUESTED -> Icons.Outlined.HourglassEmpty
    OrderTimelineStep.CONFIRMED -> Icons.Outlined.EventAvailable
    OrderTimelineStep.EN_ROUTE -> Icons.AutoMirrored.Outlined.DirectionsWalk
    OrderTimelineStep.IN_PROGRESS -> Icons.Outlined.Schedule
    OrderTimelineStep.FINISHED -> Icons.Outlined.CheckCircle
}

/** Misma secuencia para pendiente, confirmada, en camino, en curso y finalizada. Cancelada y expirada quedan afuera. */
fun orderTimeline(status: ServiceRequestStatus, bookingStatus: BookingStatus?): List<OrderTimelineStep> {
    if (status == ServiceRequestStatus.CANCELLED || status == ServiceRequestStatus.EXPIRED || bookingStatus == BookingStatus.CANCELLED || bookingStatus == BookingStatus.EXPIRED) {
        return emptyList()
    }
    return listOf(
        OrderTimelineStep.REQUESTED,
        OrderTimelineStep.CONFIRMED,
        OrderTimelineStep.EN_ROUTE,
        OrderTimelineStep.IN_PROGRESS,
        OrderTimelineStep.FINISHED,
    )
}

enum class OrderTimelinePhase { Done, Current, Upcoming }

fun orderTimelineActiveIndex(status: ServiceRequestStatus, bookingStatus: BookingStatus?): Int {
    val steps = orderTimeline(status, bookingStatus)
    if (steps.isEmpty()) return -1
    if (bookingStatus == BookingStatus.FINISHED) return steps.size
    val current = when {
        status == ServiceRequestStatus.PENDING || status == ServiceRequestStatus.COUNTER_OFFER -> OrderTimelineStep.REQUESTED
        bookingStatus == BookingStatus.EN_ROUTE -> OrderTimelineStep.EN_ROUTE
        bookingStatus == BookingStatus.IN_PROGRESS -> OrderTimelineStep.IN_PROGRESS
        else -> OrderTimelineStep.CONFIRMED
    }
    return steps.indexOf(current).coerceAtLeast(0)
}

fun orderTimelinePhases(status: ServiceRequestStatus, bookingStatus: BookingStatus?): List<OrderTimelinePhase> {
    val steps = orderTimeline(status, bookingStatus)
    val active = orderTimelineActiveIndex(status, bookingStatus)
    return steps.indices.map { index ->
        when {
            index < active -> OrderTimelinePhase.Done
            index == active -> OrderTimelinePhase.Current
            else -> OrderTimelinePhase.Upcoming
        }
    }
}

enum class ClientPrimaryAction {
    WaitingForWorker,
    ContactWorker,
    CompletionCode,
    RateWorker,
    ShowRating,
}

fun clientPrimaryAction(
    status: ServiceRequestStatus,
    bookingStatus: BookingStatus?,
    hasContact: Boolean,
    alreadyRated: Boolean,
): ClientPrimaryAction? = when (status) {
    ServiceRequestStatus.PENDING -> ClientPrimaryAction.WaitingForWorker
    ServiceRequestStatus.ACCEPTED -> when (bookingStatus) {
        BookingStatus.CONFIRMED, BookingStatus.EN_ROUTE ->
            if (hasContact) ClientPrimaryAction.ContactWorker else null
        BookingStatus.IN_PROGRESS -> ClientPrimaryAction.CompletionCode
        BookingStatus.FINISHED ->
            if (alreadyRated) ClientPrimaryAction.ShowRating else ClientPrimaryAction.RateWorker
        else -> null
    }
    else -> null
}

/** El backend guarda calidad en escala 1–10 (estrellas × 2) y puntualidad en 1–5. */
fun qualityStars(score: Int?): Int? = score?.div(2)?.coerceIn(1, 5)

fun punctualityStars(score: Int?): Int? = score?.coerceIn(1, 5)

fun orderScreenTitleRes(status: ServiceRequestStatus, bookingStatus: BookingStatus?): Int = when {
    status == ServiceRequestStatus.CANCELLED || bookingStatus == BookingStatus.CANCELLED ->
        R.string.orders_title_cancelled
    status == ServiceRequestStatus.EXPIRED || bookingStatus == BookingStatus.EXPIRED -> R.string.orders_title_cancelled
    status == ServiceRequestStatus.PENDING || status == ServiceRequestStatus.COUNTER_OFFER ->
        R.string.orders_title_pending
    bookingStatus == BookingStatus.FINISHED -> R.string.orders_title_detail
    bookingStatus == BookingStatus.IN_PROGRESS -> R.string.orders_title_finish
    else -> R.string.orders_title_confirmed
}

fun cancelledByLabelRes(cancelledBy: String?): Int? = when (cancelledBy?.uppercase()) {
    "CLIENTE" -> R.string.orders_cancelled_by_client
    "TRABAJADOR" -> R.string.orders_cancelled_by_worker
    "SISTEMA" -> R.string.orders_cancelled_by_system
    "ADMIN" -> R.string.orders_cancelled_by_support
    else -> null
}
