package com.cocido.oficios.core.common.sync

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Áreas de datos que pueden cambiar por acción de terceros (otro usuario, backoffice). */
enum class DataTopic {
    /** Solicitudes, contraofertas, bookings y su estado. */
    ORDERS,

    /** Bandeja de notificaciones y contador de no leídas. */
    NOTIFICATIONS,

    /** Estado de cuenta/perfil: revisión de documentación (KYC), validación del trabajador. */
    PROFILE_STATUS,

    /** Agenda del trabajador (turnos reservados, bloqueos). */
    AGENDA,
}

/**
 * Bus en memoria de "algo cambió del lado del servidor". Lo alimenta el push FCM cuando la
 * app está viva; las pantallas abiertas lo escuchan solo mientras están en primer plano
 * (ver `RefreshOnDataChange` en core:ui) y hacen un refresh silencioso.
 *
 * No reemplaza al refresh en ON_RESUME: si la app estaba en segundo plano o muerta, el
 * evento se pierde y el refresh al volver cubre el caso. Por eso no hay replay.
 */
object DataChangeSignals {
    private val _events = MutableSharedFlow<DataTopic>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<DataTopic> = _events.asSharedFlow()

    fun notify(topics: Collection<DataTopic>) {
        topics.forEach { _events.tryEmit(it) }
    }
}

/** Traduce el `type`/`entityType` del push (docs/backend/FCM_EVENT_CATALOG.md) a tópicos. */
object PushDataTopics {
    fun forPush(type: String?, entityType: String? = null): Set<DataTopic> {
        val normalizedType = type.orEmpty().uppercase()
        val normalizedEntity = entityType.orEmpty().uppercase()
        return buildSet {
            add(DataTopic.NOTIFICATIONS)
            val isProfile = normalizedType.startsWith("KYC") ||
                normalizedType.contains("DOCUMENT") ||
                normalizedType.contains("PERFIL") ||
                normalizedType.contains("CUENTA") ||
                normalizedEntity == "KYC" ||
                normalizedEntity == "TRABAJADOR"
            if (isProfile) add(DataTopic.PROFILE_STATUS)
            val isBooking = normalizedType.startsWith("BOOKING") ||
                normalizedType.contains("FINALIZ") ||
                normalizedEntity == "BOOKING"
            val isOrder = isBooking ||
                normalizedType.contains("SOLICITUD") ||
                normalizedType.contains("CONTRAPROPUESTA") ||
                normalizedType.startsWith("TRABAJADOR_") ||
                normalizedType.startsWith("CALIFICACION") ||
                normalizedEntity == "SOLICITUD"
            if (isOrder) add(DataTopic.ORDERS)
            if (isBooking) add(DataTopic.AGENDA)
        }
    }
}
