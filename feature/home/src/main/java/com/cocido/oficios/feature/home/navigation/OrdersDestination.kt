package com.cocido.oficios.feature.home.navigation

import android.net.Uri

object OrdersDestination {
    const val createOrder = "orders/create/{workerId}"
    const val orderDetail = "orders/{orderId}"
    const val counterOffer = "orders/{orderId}/counter-offer"
    const val rateWorker = "orders/rate/{bookingId}?workerName={workerName}"
    const val ordersList = "orders/mine"

    fun createOrder(workerId: Long): String = "orders/create/$workerId"
    fun orderDetail(orderId: Long): String = "orders/$orderId"
    fun counterOffer(orderId: Long): String = "orders/$orderId/counter-offer"
    fun rateWorker(bookingId: Long, workerName: String): String =
        "orders/rate/$bookingId?workerName=${Uri.encode(workerName)}"
}
