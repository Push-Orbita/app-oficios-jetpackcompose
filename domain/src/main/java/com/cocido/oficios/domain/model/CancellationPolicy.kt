package com.cocido.oficios.domain.model

data class CancellationPolicy(
    val canCancel: Boolean,
    val requiresReason: Boolean,
    val deadlineLabel: String?,
    val isBeforeAcceptance: Boolean,
) {
    val showDeadline: Boolean get() = !deadlineLabel.isNullOrBlank()

    fun resolveReason(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        return when {
            trimmed.isNotEmpty() -> trimmed
            requiresReason -> null
            isBeforeAcceptance -> null
            else -> DEFAULT_CLIENT_REASON
        }
    }

    companion object {
        const val REASON_REQUIRED = "Ingrese el motivo de cancelación."
        const val DEFAULT_CLIENT_REASON = "Cancelado por el cliente"
        const val WINDOW_EXPIRED_CODE = "CANCELATION_WINDOW_EXPIRED"

        fun isBookingCancellable(bookingStatus: BookingStatus?): Boolean =
            bookingStatus != BookingStatus.FINISHED &&
                bookingStatus != BookingStatus.CANCELLED &&
                bookingStatus != BookingStatus.EXPIRED

        fun forOrder(order: ServiceOrder, viewerRole: UserRole): CancellationPolicy {
            val terminalRequest = order.status == ServiceRequestStatus.CANCELLED ||
                order.status == ServiceRequestStatus.EXPIRED
            val pending = order.status == ServiceRequestStatus.PENDING
            val canCancelConfirmed = !pending &&
                !terminalRequest &&
                order.canCancelBooking &&
                isBookingCancellable(order.bookingStatus)
            val canCancel = when {
                terminalRequest -> false
                pending -> viewerRole == UserRole.CLIENT
                else -> canCancelConfirmed
            }
            return CancellationPolicy(
                canCancel = canCancel,
                requiresReason = !pending && viewerRole == UserRole.PROVIDER,
                deadlineLabel = order.cancellationDeadlineLabel,
                isBeforeAcceptance = pending && viewerRole == UserRole.CLIENT,
            )
        }
    }
}

fun ServiceOrder.cancellationPolicy(viewerRole: UserRole): CancellationPolicy =
    CancellationPolicy.forOrder(this, viewerRole)
