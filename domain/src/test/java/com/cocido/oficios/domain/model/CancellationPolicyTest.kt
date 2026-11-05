package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CancellationPolicyTest {

    @Test
    fun `confirmed booking has the same cancel window for client and worker`() {
        val order = confirmedOrder(canCancel = true, deadline = "12 oct, 18:00")
        val client = order.cancellationPolicy(UserRole.CLIENT)
        val worker = order.cancellationPolicy(UserRole.PROVIDER)
        assertTrue(client.canCancel)
        assertTrue(worker.canCancel)
        assertEquals(client.deadlineLabel, worker.deadlineLabel)
        assertTrue(client.showDeadline)
        assertTrue(worker.showDeadline)
        assertFalse(client.requiresReason)
        assertTrue(worker.requiresReason)
    }

    @Test
    fun `pending request is cancellable only by the client`() {
        val order = pendingOrder(deadline = "12 oct, 18:00")
        val client = order.cancellationPolicy(UserRole.CLIENT)
        val worker = order.cancellationPolicy(UserRole.PROVIDER)
        assertTrue(client.canCancel)
        assertTrue(client.isBeforeAcceptance)
        assertFalse(client.requiresReason)
        assertFalse(worker.canCancel)
        assertFalse(worker.isBeforeAcceptance)
        assertTrue(client.showDeadline)
        assertTrue(worker.showDeadline)
    }

    @Test
    fun `finished or expired booking cannot be cancelled by either role`() {
        for (status in listOf(BookingStatus.FINISHED, BookingStatus.CANCELLED, BookingStatus.EXPIRED)) {
            val order = confirmedOrder(canCancel = true, bookingStatus = status)
            assertFalse(order.cancellationPolicy(UserRole.CLIENT).canCancel)
            assertFalse(order.cancellationPolicy(UserRole.PROVIDER).canCancel)
        }
    }

    @Test
    fun `backend flag puedeCancelar false blocks both roles`() {
        val order = confirmedOrder(canCancel = false)
        assertFalse(order.cancellationPolicy(UserRole.CLIENT).canCancel)
        assertFalse(order.cancellationPolicy(UserRole.PROVIDER).canCancel)
    }

    @Test
    fun `deadline stays visible even when the window already expired`() {
        val order = confirmedOrder(canCancel = false, deadline = "10 oct, 09:00")
        val client = order.cancellationPolicy(UserRole.CLIENT)
        val worker = order.cancellationPolicy(UserRole.PROVIDER)
        assertFalse(client.canCancel)
        assertFalse(worker.canCancel)
        assertTrue(client.showDeadline)
        assertTrue(worker.showDeadline)
        assertEquals("10 oct, 09:00", client.deadlineLabel)
    }

    @Test
    fun `worker reason is required and client falls back to a default`() {
        val order = confirmedOrder(canCancel = true)
        val client = order.cancellationPolicy(UserRole.CLIENT)
        val worker = order.cancellationPolicy(UserRole.PROVIDER)
        assertEquals(CancellationPolicy.DEFAULT_CLIENT_REASON, client.resolveReason("  "))
        assertEquals("Cambio de agenda", client.resolveReason(" Cambio de agenda "))
        assertNull(worker.resolveReason("  "))
        assertEquals("No llego", worker.resolveReason("No llego"))
    }

    @Test
    fun `isBookingCancellable matches terminal booking statuses`() {
        assertTrue(CancellationPolicy.isBookingCancellable(BookingStatus.CONFIRMED))
        assertTrue(CancellationPolicy.isBookingCancellable(BookingStatus.EN_ROUTE))
        assertTrue(CancellationPolicy.isBookingCancellable(null))
        assertFalse(CancellationPolicy.isBookingCancellable(BookingStatus.FINISHED))
        assertFalse(CancellationPolicy.isBookingCancellable(BookingStatus.CANCELLED))
        assertFalse(CancellationPolicy.isBookingCancellable(BookingStatus.EXPIRED))
    }

    private fun pendingOrder(deadline: String? = null) = ServiceOrder(
        id = 1L,
        clientName = "Ana",
        workerName = "Juan",
        workerId = 2L,
        rubroLabel = "Hogar",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.PENDING,
        description = "",
        addressLabel = "Calle 1",
        contactName = "Ana",
        contactPhone = "3704000000",
        cancellationDeadlineLabel = deadline,
    )

    private fun confirmedOrder(
        canCancel: Boolean,
        deadline: String? = null,
        bookingStatus: BookingStatus = BookingStatus.CONFIRMED,
    ) = ServiceOrder(
        id = 1L,
        clientName = "Ana",
        workerName = "Juan",
        workerId = 2L,
        rubroLabel = "Hogar",
        subRubroLabel = "Plomería",
        status = ServiceRequestStatus.ACCEPTED,
        description = "",
        addressLabel = "Calle 1",
        contactName = "Ana",
        contactPhone = "3704000000",
        bookingStatus = bookingStatus,
        canCancelBooking = canCancel,
        cancellationDeadlineLabel = deadline,
    )
}
