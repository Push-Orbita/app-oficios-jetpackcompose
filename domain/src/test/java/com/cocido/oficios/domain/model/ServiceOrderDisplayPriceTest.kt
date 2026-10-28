package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceOrderDisplayPriceTest {

    @Test
    fun `pending counter offer price wins over accepted`() {
        val order = sampleOrder(
            pendingCounterOffer = CounterOffer(1, "4 mar", 40_000.0, CounterOfferStatus.PENDING),
            acceptedPrice = 50_000.0,
        )
        assertEquals(40_000.0, order.displayPrice())
    }

    @Test
    fun `accepted price is used when there is no pending offer`() {
        val order = sampleOrder(acceptedPrice = 50_000.0)
        assertEquals(50_000.0, order.displayPrice())
    }

    @Test
    fun `no price when backend did not send nuevoPrecio`() {
        assertNull(sampleOrder().displayPrice())
    }

    private fun sampleOrder(
        pendingCounterOffer: CounterOffer? = null,
        acceptedPrice: Double? = null,
    ) = ServiceOrder(
        id = 1L,
        clientName = "Cliente",
        workerName = "Trabajador",
        workerId = 2L,
        rubroLabel = "Albañilería",
        subRubroLabel = "Revoque",
        status = ServiceRequestStatus.ACCEPTED,
        description = "Revoque",
        addressLabel = "Calle 1",
        contactName = "Cliente",
        contactPhone = "3704000000",
        pendingCounterOffer = pendingCounterOffer,
        acceptedPrice = acceptedPrice,
    )
}
