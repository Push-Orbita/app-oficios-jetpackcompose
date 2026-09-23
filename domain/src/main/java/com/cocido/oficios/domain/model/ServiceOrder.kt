package com.cocido.oficios.domain.model

// Estados reales del backend (apioficios.pushsoftware.com.ar) — ver /solicitud-servicio,
// /booking. No inventar estados nuevos: mapear 1:1 contra estos.
enum class ServiceRequestStatus {
    PENDING,
    COUNTER_OFFER,
    ACCEPTED,
    CANCELLED,
    EXPIRED,
}

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    EN_ROUTE,
    IN_PROGRESS,
    FINISHED,
    CANCELLED,
}

enum class CounterOfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

data class ServiceOrder(
    val id: Long,
    val clientName: String,
    val workerName: String,
    val workerId: Long,
    val rubroLabel: String,
    val subRubroLabel: String,
    val status: ServiceRequestStatus,
    val description: String,
    val addressLabel: String,
    val addressLatitude: Double? = null,
    val addressLongitude: Double? = null,
    val contactName: String,
    val contactPhone: String,
    val rejectionReason: String? = null,
    val bookingId: Long? = null,
    val scheduledLabel: String? = null,
    val bookingStatus: BookingStatus? = null,
    val bookingType: String? = null,
    val canCancelBooking: Boolean = false,
    val pendingCounterOffer: CounterOffer? = null,
    val alreadyRated: Boolean = false,
    val completionCode: String? = null,
)

data class BookingTracking(
    val bookingId: Long,
    val workerId: Long,
    val status: String,
    val location: GeoPoint,
    val updatedAtIso: String,
)

data class CounterOffer(
    val id: Long,
    val proposedLabel: String,
    val proposedPrice: Double?,
    val status: CounterOfferStatus,
)

data class ServiceOrderListItem(
    val id: Long,
    val counterpartName: String,
    val subRubroLabel: String,
    val status: ServiceRequestStatus,
    val bookingStatus: BookingStatus?,
    val scheduledLabel: String?,
    val addressLabel: String,
)

data class CreateServiceRequestParams(
    val trabajadorOficioSubRubroId: Long,
    val domicilioId: Long,
    val contactoId: Long,
    val slotTurnoId: Long? = null,
    val proposedStartIso: String? = null,
    val proposedEndIso: String? = null,
    val description: String?,
    val idempotencyKey: String,
)
