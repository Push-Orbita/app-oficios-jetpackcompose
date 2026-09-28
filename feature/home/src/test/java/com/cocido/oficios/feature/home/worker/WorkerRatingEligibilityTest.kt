package com.cocido.oficios.feature.home.worker

import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.ServiceRequestStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkerRatingEligibilityTest {

    @Test
    fun `calificar solo si hay un trabajo finalizado sin reseña con ese trabajador`() {
        val orders = listOf(
            order(workerId = 4, bookingId = 10, bookingStatus = BookingStatus.FINISHED),
            order(workerId = 4, bookingId = 11, bookingStatus = BookingStatus.IN_PROGRESS),
            order(workerId = 9, bookingId = 12, bookingStatus = BookingStatus.FINISHED),
        )
        assertEquals(10L, pendingRatingBookingId(workerId = 4, orders = orders, ratedBookingIds = emptySet()))
        assertNull(pendingRatingBookingId(workerId = 4, orders = orders, ratedBookingIds = setOf(10L)))
        assertNull(pendingRatingBookingId(workerId = 3, orders = orders, ratedBookingIds = emptySet()))
    }

    private fun order(
        workerId: Long,
        bookingId: Long,
        bookingStatus: BookingStatus,
    ) = ServiceOrderListItem(
        id = bookingId,
        counterpartName = "Ana",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.ACCEPTED,
        bookingStatus = bookingStatus,
        scheduledLabel = null,
        addressLabel = "",
        workerId = workerId,
        bookingId = bookingId,
    )
}
