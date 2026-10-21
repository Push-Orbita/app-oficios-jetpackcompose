package com.cocido.oficios.feature.home.notifications

import androidx.annotation.DrawableRes
import com.cocido.oficios.core.designsystem.component.OficiosIcons

/**
 * Ícono Lucide del set de la app según el `type` que ya manda el backend.
 * Tipos del catálogo FCM v1 + variantes de cancelación vistas en prod.
 */
@DrawableRes
internal fun notificationIconRes(type: String): Int {
    val normalized = type.trim().uppercase()
    return when (normalized) {
        "NUEVA_SOLICITUD" -> OficiosIcons.CirclePlus
        "SOLICITUD_ACEPTADA",
        "CONTRAPROPUESTA_ACEPTADA",
        "BOOKING_CONFIRMADO",
        "SERVICIO_FINALIZADO",
        -> OficiosIcons.CircleCheck
        "SOLICITUD_RECHAZADA",
        "SOLICITUD_CANCELADA",
        "BOOKING_CANCELADO",
        -> OficiosIcons.CircleAlert
        "CONTRAPROPUESTA_RECIBIDA" -> OficiosIcons.ScrollText
        "TRABAJADOR_EN_CAMINO",
        "TRABAJADOR_LLEGO",
        -> OficiosIcons.MapPinHouse
        "CALIFICACION_PENDIENTE" -> OficiosIcons.Star
        "KYC_REVISION_DOCUMENTACION" -> OficiosIcons.ShieldCheck
        "BOOKING",
        "SOLICITUD",
        -> OficiosIcons.ReceiptText
        else -> when {
            "CANCEL" in normalized -> OficiosIcons.CircleAlert
            else -> OficiosIcons.BellDot
        }
    }
}
