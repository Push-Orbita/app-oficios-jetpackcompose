package com.cocido.oficios.core.designsystem.component.cards

// ---------------------------------------------------------------------------
// OrderStatus — estados semánticos de una orden/pedido
// Usados por: OrderCard, OficiosStatusChip
// ---------------------------------------------------------------------------

sealed class OrderStatus {
    object Pending : OrderStatus()
    object Confirmed : OrderStatus()
    object InReview : OrderStatus()
    object Approved : OrderStatus()
    object Completed : OrderStatus()
    object Rejected : OrderStatus()
    object Cancelled : OrderStatus()

    fun label(): String = when (this) {
        is Pending -> "Pendiente"
        is Confirmed -> "Confirmado"
        is InReview -> "En revisión"
        is Approved -> "Aprobado"
        is Completed -> "Completado"
        is Rejected -> "Rechazado"
        is Cancelled -> "Cancelado"
    }
}
