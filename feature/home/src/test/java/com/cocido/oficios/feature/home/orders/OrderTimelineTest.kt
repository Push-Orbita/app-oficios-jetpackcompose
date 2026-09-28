package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderTimelineTest {

    private val sequence = listOf(
        OrderTimelineStep.REQUESTED,
        OrderTimelineStep.CONFIRMED,
        OrderTimelineStep.EN_ROUTE,
        OrderTimelineStep.IN_PROGRESS,
        OrderTimelineStep.FINISHED,
    )

    @Test
    fun `pendiente confirmada en camino en curso y finalizada comparten la secuencia`() {
        assertEquals(sequence, orderTimeline(ServiceRequestStatus.PENDING, null))
        assertEquals(sequence, orderTimeline(ServiceRequestStatus.ACCEPTED, BookingStatus.CONFIRMED))
        assertEquals(sequence, orderTimeline(ServiceRequestStatus.ACCEPTED, BookingStatus.EN_ROUTE))
        assertEquals(sequence, orderTimeline(ServiceRequestStatus.ACCEPTED, BookingStatus.IN_PROGRESS))
        assertEquals(sequence, orderTimeline(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED))
    }

    @Test
    fun `cancelada y expirada quedan fuera del stepper`() {
        assertEquals(emptyList<OrderTimelineStep>(), orderTimeline(ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED))
        assertEquals(emptyList<OrderTimelineStep>(), orderTimeline(ServiceRequestStatus.EXPIRED, null))
        assertEquals(emptyList<OrderTimelineStep>(), orderTimeline(ServiceRequestStatus.ACCEPTED, BookingStatus.CANCELLED))
    }

    @Test
    fun `el paso actual sigue el estado`() {
        assertEquals(0, orderTimelineActiveIndex(ServiceRequestStatus.PENDING, null))
        assertEquals(1, orderTimelineActiveIndex(ServiceRequestStatus.ACCEPTED, BookingStatus.CONFIRMED))
        assertEquals(2, orderTimelineActiveIndex(ServiceRequestStatus.ACCEPTED, BookingStatus.EN_ROUTE))
        assertEquals(3, orderTimelineActiveIndex(ServiceRequestStatus.ACCEPTED, BookingStatus.IN_PROGRESS))
        assertEquals(sequence.size, orderTimelineActiveIndex(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED))
        assertEquals(-1, orderTimelineActiveIndex(ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED))
    }

    @Test
    fun `finalizada deja todos los pasos completados`() {
        val phases = orderTimelinePhases(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED)
        assertEquals(List(sequence.size) { OrderTimelinePhase.Done }, phases)
        assertTrue(phases.none { it == OrderTimelinePhase.Current })
    }

    @Test
    fun `la accion principal del cliente sigue el estado`() {
        assertEquals(
            ClientPrimaryAction.WaitingForWorker,
            clientPrimaryAction(ServiceRequestStatus.PENDING, null, hasContact = true, alreadyRated = false),
        )
        assertEquals(
            ClientPrimaryAction.ContactWorker,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.CONFIRMED, hasContact = true, alreadyRated = false),
        )
        assertEquals(
            ClientPrimaryAction.ContactWorker,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.EN_ROUTE, hasContact = true, alreadyRated = false),
        )
        assertEquals(
            null,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.EN_ROUTE, hasContact = false, alreadyRated = false),
        )
        assertEquals(
            ClientPrimaryAction.CompletionCode,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.IN_PROGRESS, hasContact = true, alreadyRated = false),
        )
        assertEquals(
            ClientPrimaryAction.RateWorker,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED, hasContact = false, alreadyRated = false),
        )
        assertEquals(
            ClientPrimaryAction.ShowRating,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED, hasContact = false, alreadyRated = true),
        )
        assertEquals(
            null,
            clientPrimaryAction(ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED, hasContact = true, alreadyRated = false),
        )
    }

    @Test
    fun `cancelada no arma stepper`() {
        assertTrue(orderTimeline(ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED).isEmpty())
        assertTrue(orderTimelinePhases(ServiceRequestStatus.CANCELLED, BookingStatus.CANCELLED).isEmpty())
        assertTrue(orderTimelinePhases(ServiceRequestStatus.EXPIRED, null).isEmpty())
    }
}
