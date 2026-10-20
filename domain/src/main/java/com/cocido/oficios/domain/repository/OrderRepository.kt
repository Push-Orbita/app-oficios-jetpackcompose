package com.cocido.oficios.domain.repository

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.BookingTracking
import com.cocido.oficios.domain.model.GeoPoint
import kotlinx.coroutines.flow.SharedFlow

interface OrderRepository {
    val ordersChanged: SharedFlow<Unit>
    suspend fun createServiceRequest(params: CreateServiceRequestParams): AppResult<Long>
    suspend fun getClientOrders(): AppResult<List<ServiceOrderListItem>>
    suspend fun getWorkerOrders(): AppResult<List<ServiceOrderListItem>>
    suspend fun getOrderDetail(orderId: Long): AppResult<ServiceOrder>
    suspend fun acceptOrder(orderId: Long): AppResult<Unit>
    suspend fun rejectOrder(orderId: Long, reason: String): AppResult<Unit>
    suspend fun sendCounterOffer(
        orderId: Long,
        newStartIso: String,
        newEndIso: String,
        newPrice: Double?,
    ): AppResult<Unit>
    suspend fun acceptCounterOffer(counterOfferId: Long): AppResult<Unit>
    suspend fun rejectCounterOffer(counterOfferId: Long): AppResult<Unit>
    suspend fun cancelOrderBeforeAcceptance(orderId: Long): AppResult<Unit>
    suspend fun cancelConfirmedOrder(orderId: Long, reason: String?): AppResult<Unit>
    suspend fun getBookingTracking(bookingId: Long): AppResult<BookingTracking?>
    suspend fun findOrderIdForBooking(bookingId: Long): AppResult<Long?>
    suspend fun markEnRoute(bookingId: Long, location: GeoPoint): AppResult<Unit>
    suspend fun markArrived(bookingId: Long, location: GeoPoint): AppResult<Unit>
    /** Código vigente que ve el cliente (GET /booking/{id}); null si el booking ya no está EN_CURSO. */
    suspend fun getCompletionCode(bookingId: Long): AppResult<String?>
    /** Regenera el código del cliente. Puede fallar con 429 COMPLETION_CODE_RATE_LIMITED. */
    suspend fun resendCompletionCode(bookingId: Long): AppResult<Unit>
    suspend fun finalizeBooking(bookingId: Long, completionCode: String): AppResult<Unit>
    suspend fun submitRating(bookingId: Long, quality: Int, punctuality: Int, comment: String?): AppResult<Unit>
}
