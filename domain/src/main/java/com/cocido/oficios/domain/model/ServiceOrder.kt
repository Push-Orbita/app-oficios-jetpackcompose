package com.cocido.oficios.domain.model

import java.time.ZonedDateTime

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
    EXPIRED,
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
    /** Teléfono del trabajador, si el perfil o el oficio lo traen. Nunca el contacto del cliente. */
    val workerPhone: String? = null,
    val rejectionReason: String? = null,
    val bookingId: Long? = null,
    val scheduledLabel: String? = null,
    val bookingStatus: BookingStatus? = null,
    val bookingType: String? = null,
    val canCancelBooking: Boolean = false,
    /** `fechaLimiteCancelacion` del booking, ya formateada. */
    val cancellationDeadlineLabel: String? = null,
    val pendingCounterOffer: CounterOffer? = null,
    /** `nuevoPrecio` de una contrapropuesta ACEPTADA, si el backend la envía. */
    val acceptedPrice: Double? = null,
    val alreadyRated: Boolean = false,
    val qualityScore: Int? = null,
    val punctualityScore: Int? = null,
    val completionCode: String? = null,
    val workerAvatarUrl: String? = null,
    val clientAvatarUrl: String? = null,
    val cancelledBy: String? = null,
    val cancellationReason: String? = null,
)

data class BookingTracking(
    val bookingId: Long,
    val workerId: Long,
    val status: String,
    val location: GeoPoint?,
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
    val rubroLabel: String = "",
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val workerId: Long = 0L,
    val bookingId: Long? = null,
    val scheduledSortKey: String? = null,
    val scheduledEndKey: String? = null,
    val scheduledDurationMinutes: Int? = null,
)

enum class OrdersListFilter {
    ACTIVE,
    HISTORY,
}

enum class OrdersListSort {
    NEWEST_FIRST,
    OLDEST_FIRST,
}

data class OrdersListSortPrefs(
    val active: OrdersListSort = OrdersListSort.NEWEST_FIRST,
    val history: OrdersListSort = OrdersListSort.NEWEST_FIRST,
) {
    fun forFilter(filter: OrdersListFilter): OrdersListSort = when (filter) {
        OrdersListFilter.ACTIVE -> active
        OrdersListFilter.HISTORY -> history
    }

    fun with(filter: OrdersListFilter, sort: OrdersListSort): OrdersListSortPrefs = when (filter) {
        OrdersListFilter.ACTIVE -> copy(active = sort)
        OrdersListFilter.HISTORY -> copy(history = sort)
    }
}

fun ordersListComparator(sort: OrdersListSort): Comparator<ServiceOrderListItem> {
    val missingDatesLast = compareBy<ServiceOrderListItem> { it.scheduledSortKey == null }
    val byBookingDate = when (sort) {
        OrdersListSort.NEWEST_FIRST ->
            compareByDescending<ServiceOrderListItem> { it.scheduledSortKey.orEmpty() }
        OrdersListSort.OLDEST_FIRST ->
            compareBy<ServiceOrderListItem> { it.scheduledSortKey.orEmpty() }
    }
    return missingDatesLast.then(byBookingDate).thenByDescending { it.id }
}

/**
 * Historial: solicitud cancelada/expirada, booking FINALIZADA/CANCELADA/EXPIRADA,
 * o PENDIENTE/CONTRAPROPUESTA/CONFIRMADA cuya franja ya terminó.
 * EN_CAMINO / EN_CURSO (y LLEGUE mapeado a en curso) siguen en Activos
 * aunque el horario haya pasado: el trabajo está en marcha.
 * No inventa estados: el ítem conserva el estado real del backend.
 */
fun ServiceOrderListItem.isHistoryOrder(now: ZonedDateTime): Boolean {
    if (status == ServiceRequestStatus.CANCELLED || status == ServiceRequestStatus.EXPIRED) return true
    if (bookingStatus == BookingStatus.FINISHED || bookingStatus == BookingStatus.CANCELLED || bookingStatus == BookingStatus.EXPIRED) return true
    if (bookingStatus == BookingStatus.EN_ROUTE || bookingStatus == BookingStatus.IN_PROGRESS) return false
    return isElapsedUnstartedSlot(now)
}

/** True si hay fecha/hora y el fin exclusivo de la franja ya quedó atrás. */
fun ServiceOrderListItem.isElapsedUnstartedSlot(now: ZonedDateTime): Boolean {
    val end = agendaSlotEndExclusive(now.zone) ?: return false
    return !now.isBefore(end)
}

/** Precio a mostrar: contrapropuesta pendiente o la ya aceptada. No hay monto ni medio de pago en el booking. */
fun ServiceOrder.displayPrice(): Double? = pendingCounterOffer?.proposedPrice ?: acceptedPrice

/** Coordenadas del domicilio del pedido, si el backend mandó un punto válido. */
fun ServiceOrder.destinationPoint(): GeoPoint? {
    val latitude = addressLatitude ?: return null
    val longitude = addressLongitude ?: return null
    return GeoPoint(latitude, longitude).takeIf { it.isValid }
}

/**
 * Pin del trabajador solo en EN_CAMINO: en curso ya llegó al domicilio
 * y un GPS de emulador no debe tapar la dirección del pedido.
 */
fun ServiceOrder.enRouteWorkerPoint(tracking: BookingTracking?): GeoPoint? {
    if (bookingStatus != BookingStatus.EN_ROUTE) return null
    return tracking?.location?.takeIf { it.isValid }
}

/**
 * Recientes del home cliente: trabajadores a los que ya se les pidió un servicio.
 * Más reciente primero, un ítem por trabajador, sin ids vacíos.
 */
fun List<ServiceOrderListItem>.recentClientWorkers(limit: Int = 8): List<WorkerSummary> {
    val newestFirst = compareByDescending<ServiceOrderListItem, String?>(nullsLast()) { it.scheduledSortKey }
        .thenByDescending { it.id }
    return filter { it.workerId > 0L && it.counterpartName.isNotBlank() }
        .sortedWith(newestFirst)
        .distinctBy { it.workerId }
        .take(limit)
        .map { item ->
            WorkerSummary(
                id = item.workerId,
                displayName = item.counterpartName,
                specialtyLabel = item.rubroLabel.ifBlank { item.subRubroLabel },
                rating = null,
                avatarUrl = item.avatarUrl,
                isVerified = item.isVerified,
                primaryRubroId = null,
            )
        }
}

fun ServiceOrder.withArrivedTracking(trackingStatus: String?): ServiceOrder {
    if (!trackingStatus.orEmpty().trim().equals("LLEGUE", ignoreCase = true)) return this
    if (status == ServiceRequestStatus.CANCELLED || status == ServiceRequestStatus.EXPIRED) return this
    if (bookingStatus == BookingStatus.FINISHED || bookingStatus == BookingStatus.CANCELLED || bookingStatus == BookingStatus.EXPIRED) return this
    return copy(bookingStatus = BookingStatus.IN_PROGRESS)
}

fun List<ServiceOrderListItem>.arrangedFor(
    filter: OrdersListFilter,
    now: ZonedDateTime,
    sort: OrdersListSort = OrdersListSort.NEWEST_FIRST,
): List<ServiceOrderListItem> {
    val matching = filter { item ->
        if (filter == OrdersListFilter.HISTORY) item.isHistoryOrder(now) else !item.isHistoryOrder(now)
    }
    return matching.sortedWith(ordersListComparator(sort))
}

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
