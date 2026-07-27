package com.cocido.oficios.domain.usecase

import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.repository.OrderRepository
import javax.inject.Inject

class CreateServiceRequestUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(params: CreateServiceRequestParams) =
        orderRepository.createServiceRequest(params)
}

/**
 * Agrupa el resto de las acciones sobre un pedido ya creado — mismo patrón que
 * GetOnboardingStatusUseCase, evita tener una clase de una sola línea por cada endpoint.
 */
class OrderUseCases @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend fun getClientOrders() = orderRepository.getClientOrders()
    suspend fun getWorkerOrders() = orderRepository.getWorkerOrders()
    suspend fun getOrderDetail(orderId: Long) = orderRepository.getOrderDetail(orderId)
    suspend fun acceptOrder(orderId: Long) = orderRepository.acceptOrder(orderId)
    suspend fun rejectOrder(orderId: Long, reason: String) = orderRepository.rejectOrder(orderId, reason)
    suspend fun sendCounterOffer(orderId: Long, newStartIso: String, newEndIso: String, newPrice: Double?) =
        orderRepository.sendCounterOffer(orderId, newStartIso, newEndIso, newPrice)
    suspend fun acceptCounterOffer(counterOfferId: Long) = orderRepository.acceptCounterOffer(counterOfferId)
    suspend fun rejectCounterOffer(counterOfferId: Long) = orderRepository.rejectCounterOffer(counterOfferId)
    suspend fun cancelOrderBeforeAcceptance(orderId: Long) = orderRepository.cancelOrderBeforeAcceptance(orderId)
    suspend fun markEnRoute(bookingId: Long) = orderRepository.markEnRoute(bookingId)
    suspend fun markArrived(bookingId: Long) = orderRepository.markArrived(bookingId)
    suspend fun finalizeBooking(bookingId: Long) = orderRepository.finalizeBooking(bookingId)
    suspend fun submitRating(bookingId: Long, quality: Int, punctuality: Int, comment: String?) =
        orderRepository.submitRating(bookingId, quality, punctuality, comment)
}
