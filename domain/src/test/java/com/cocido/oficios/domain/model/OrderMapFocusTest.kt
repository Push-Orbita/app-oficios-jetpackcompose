package com.cocido.oficios.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderMapFocusTest {

    @Test
    fun `destination rejects missing or zero coordinates`() {
        assertNull(order(lat = null, lng = null).destinationPoint())
        assertNull(order(lat = 0.0, lng = 0.0).destinationPoint())
        assertEquals(-26.185, order(lat = -26.185, lng = -58.175).destinationPoint()?.latitude)
    }

    @Test
    fun `worker pin only overlays while en route`() {
        val tracking = BookingTracking(
            bookingId = 1,
            workerId = 2,
            status = "EN_CAMINO",
            location = GeoPoint(37.422, -122.084),
            updatedAtIso = "2026-09-30T10:00:00Z",
        )
        assertNull(
            order(booking = BookingStatus.IN_PROGRESS).enRouteWorkerPoint(tracking),
        )
        assertEquals(
            37.422,
            order(booking = BookingStatus.EN_ROUTE).enRouteWorkerPoint(tracking)?.latitude,
        )
    }

    private fun order(
        lat: Double? = -26.185,
        lng: Double? = -58.175,
        booking: BookingStatus = BookingStatus.IN_PROGRESS,
    ) = ServiceOrder(
        id = 1,
        clientName = "Cliente",
        workerName = "Trabajador",
        workerId = 2,
        rubroLabel = "Plomería",
        subRubroLabel = "Cañerías",
        status = ServiceRequestStatus.ACCEPTED,
        description = "",
        addressLabel = "Av. 25 de Mayo 450",
        addressLatitude = lat,
        addressLongitude = lng,
        contactName = "Cliente",
        contactPhone = "3705000000",
        bookingStatus = booking,
    )
}
